import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.intellij.codeInspection.*;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MySpellChecking extends LocalInspectionTool {
    private static boolean isDemo; // todo использовать функционал демо по-умолчанию
    private static boolean apiEnabled = false;
    private static JS_API jsAPI;
    private static HTTP_API httpAPI;
    private static Map<String, ArrayList<ProblemInfo>> hashMapCommentText;
    private String balloonHint = "";

    MySpellChecking() {
        AppSettingsState state = AppSettingsState.getInstance();
        if (state.hashMapCommentText == null) {
            state.hashMapCommentText = new HashMap<String, ArrayList<ProblemInfo>>();
        }
        hashMapCommentText = state.hashMapCommentText;
        isDemo = state.isDemo;

        if (state.isDemo) {
            jsAPI = new JS_API();
            apiEnabled = true;
            balloonHint = "Внимание! Используется Демо режим";
        } else if (state.glvrdAPIKey.length() == 0) {
            balloonHint = "Внимание! Ключ лицензии не установлен";
        } else {
            httpAPI = new HTTP_API(state.glvrdAPIKey);
            try {
                var glvrdStatus = httpAPI.status();
                if (glvrdStatus.period_underlimit) {
                    apiEnabled = true;
                } else {
                    balloonHint = "Внимание! HTTP API недоступно";
                }
            } catch (Exception e) {
                balloonHint = "Внимание! HTTP API недоступно";
                e.printStackTrace();
            }
        }
    }

    @Override
    public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
        return super.getBatchSuppressActions(element);
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        if (FileContextUtil.getContextFile(element) != element.getContainingFile()) {
            return true;
        }
        return super.isSuppressedFor(element);
    }

    private boolean isCyrillicText(String text) {
        return text.chars()
                .mapToObj(Character.UnicodeBlock::of)
                .anyMatch(b -> b.equals(Character.UnicodeBlock.CYRILLIC));
    }

    @Override
    public void inspectionStarted(@NotNull LocalInspectionToolSession session, boolean isOnTheFly) {
        if (balloonHint.length() != 0) {
            NotificationGroupManager.getInstance().getNotificationGroup("License Group")
                    .createNotification(balloonHint, NotificationType.WARNING)
                    .notify(session.getFile().getProject());
            balloonHint = "";
        }
    }

    @Override
    public void inspectionFinished(@NotNull LocalInspectionToolSession session, @NotNull ProblemsHolder problemsHolder) {
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly, final LocalInspectionToolSession session) {
        if (!apiEnabled) {
            return super.buildVisitor(holder, isOnTheFly, session);
        }
        final var original = ProgressManager.getInstance().getProgressIndicator();
        if (original != null) {
            if (original.isCanceled()) {
                return super.buildVisitor(holder, isOnTheFly, session);
            }
        }
        final var indicator = new EmptyProgressIndicator();

        return new PsiElementVisitor() {
            private ArrayList<ProblemInfo> httpCheck(PsiComment psiComment) throws Exception {
                final var elementText = psiComment.getText();
                final var elementKey = elementText.trim();
                final var problems = new ArrayList<ProblemInfo>();
                var map = httpAPI.proofread(elementText);

                if (map != null) {
                    NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                            .createNotification("Result code: " + elementKey + " " + map.score, NotificationType.INFORMATION)
                            .notify(psiComment.getProject());

                    if (!map.fragments.isEmpty()) {
                        for (Fragment glvrdFragment : map.fragments) {
                            final var hintText = httpAPI.hints(glvrdFragment.hint_id);
                            final var hintData = hintText.hints.get(glvrdFragment.hint_id);
                            final var desc = String.format("GLVRD: %s", hintData.get("name").asText());
                            final var problemInfo = new ProblemInfo(desc, glvrdFragment.start, glvrdFragment.end);
                            problems.add(problemInfo);
                        }
                    }
                }

                hashMapCommentText.put(elementKey, problems);

                return problems;
            }

            private ArrayList<ProblemInfo> jsCheck(PsiComment psiComment) throws Exception {
                final var elementText = psiComment.getText();
                final var elementKey = elementText.trim();
                final var problems = new ArrayList<ProblemInfo>();
                var map = jsAPI.proofread(elementText);

                if (map != null) {
                    NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                            .createNotification("Result code: " + elementKey + " " + map.score, NotificationType.INFORMATION)
                            .notify(psiComment.getProject());

                    if (!map.fragments.isEmpty()) {
                        for (var fragment : map.fragments) {
                            for (var innerFragment : fragment) {
                                final var hintData = map.hints.get(innerFragment.hint);
                                final var desc = String.format("GLVRD: %s.\n%s", hintData.get("name").asText(), hintData.get("short_description").asText());
                                final var problemInfo = new ProblemInfo(desc, innerFragment.start, innerFragment.end);
                                problems.add(problemInfo);
                            }
                        }
                    }
                }
                hashMapCommentText.put(elementKey, problems);

                return problems;
            }

            @Override
            public void visitComment(@NotNull final PsiComment psiComment) {
                final var elementText = psiComment.getText();
                final var elementKey = elementText.trim();
                final var self = this;
                // cancel For SmallText
                if (elementKey.length() < 9) {
                    return;
                }
                // only russian text
                if (!isCyrillicText(elementKey)) {
                    return;
                }
                // todo тестово перенес сюда, если что-то не работает - перенести чуть ниже
                if (indicator.isRunning()) {
                    return;
                }
                // забираем объекты из кэша
                if (hashMapCommentText.containsKey(elementKey)) {
                    final var problems = hashMapCommentText.get(elementKey);
                    if (problems.isEmpty()) {
                        return;
                    }
                    for (var problem : problems) {
                        if (problem != null) {
                            TextRange textRange = new TextRange(problem.fragmentStart, problem.fragmentEnd);
                            String descriptionTemplate = problem.descriptionTemplate;
                            holder.registerProblem(psiComment, textRange, descriptionTemplate, LocalQuickFix.EMPTY_ARRAY);
                        }
                    }
                    return;
                }

                Task.Backgroundable backgroundable = new Task.Backgroundable(psiComment.getProject(), elementKey, false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
                    public void onSuccess() {
                    }

                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            var problems = isDemo ? self.jsCheck(psiComment) : self.httpCheck(psiComment);

                            Runnable onEnd = () -> {
                                for (var problem : problems) {
                                    final var textRange = new TextRange(problem.fragmentStart, problem.fragmentEnd);

                                    // todo почему-то не происходит обновления, требуя переход на новую строку
                                    holder.registerProblem(psiComment, textRange, problem.descriptionTemplate, LocalQuickFix.EMPTY_ARRAY);
                                }
                                if (indicator.isRunning()) {
                                    indicator.stop();
                                }
                            };

                            ApplicationManager.getApplication().invokeLater(onEnd, ModalityState.defaultModalityState());
                        } catch (Exception e) {
                            e.printStackTrace();
                            NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                    .createNotification(e.toString(), NotificationType.INFORMATION)
                                    .notify(psiComment.getProject());
                        }
                    }
                };

                ProgressManager.getInstance().runProcessWithProgressAsynchronously(backgroundable, indicator);
            }
        };
    }
}
