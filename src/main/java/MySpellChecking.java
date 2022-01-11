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
    private static GLVRD glvrd;
    private static Map<String, ArrayList<ProblemInfo>> hashMapCommentText;

    MySpellChecking() {
        AppSettingsState state = AppSettingsState.getInstance();
        if (state.hashMapCommentText == null) {
            state.hashMapCommentText = new HashMap<String, ArrayList<ProblemInfo>>();
        }
        hashMapCommentText = state.hashMapCommentText;
        glvrd = new GLVRD(state.glvrdAPIKey);
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

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly, final LocalInspectionToolSession session) {
        final var original = ProgressManager.getInstance().getProgressIndicator();
        if (original != null) {
            if (original.isCanceled()) {
                return super.buildVisitor(holder, isOnTheFly, session);
            }
        }
        final var indicator = new EmptyProgressIndicator();

        return new PsiElementVisitor() {
            @Override
            public void visitComment(@NotNull final PsiComment psiComment) {
                // cancelForSmallText
                final var elementText = psiComment.getText();
                final var elementKey = elementText.trim();
                if (elementKey.length() < 5) {
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

                if (indicator.isRunning()) {
                    return;
                }

                Task.Backgroundable backgroundable = new Task.Backgroundable(psiComment.getProject(), elementKey) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GlvrdResponse map = null;
                        final var problems = new ArrayList<ProblemInfo>();
                        try {
                            map = glvrd.proofRead(elementText);
                            if (!map.fragments.isEmpty()) {
                                for (Fragment glvrdFragment : map.fragments) {
                                    final var hintText = glvrd.hints(glvrdFragment.hint_id);
                                    final var hintData = hintText.hints.get(glvrdFragment.hint_id);
                                    final var desc = String.format("GLVRD: %s", hintData.get("name").asText());
                                    final var problemInfo = new ProblemInfo(desc, glvrdFragment.start, glvrdFragment.end);
                                    problems.add(problemInfo);
                                }
                            }
                            hashMapCommentText.put(elementKey, problems);
                        } catch (Exception e) {
//                            e.printStackTrace();
                            NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                    .createNotification(e.toString(), NotificationType.INFORMATION)
                                    .notify(psiComment.getProject());
                        }

                        GlvrdResponse finalMap = map;
                        Runnable onEnd = () -> {
                            for (var problem : problems) {
                                final var textRange = new TextRange(problem.fragmentStart, problem.fragmentEnd);
                                // todo почему-то не происходит обновления, требуя переход на новую строку
                                holder.registerProblem(psiComment, textRange, problem.descriptionTemplate, LocalQuickFix.EMPTY_ARRAY);
                            }
                            if (finalMap != null) {
                                NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                        .createNotification("Result code: " + elementKey + " " + finalMap.score, NotificationType.INFORMATION)
                                        .notify(psiComment.getProject());
                            }
                            if (indicator.isRunning()) {
                                indicator.stop();
                                indicator.notify();
                            }
                        };

                        ApplicationManager.getApplication().invokeLater(onEnd, ModalityState.NON_MODAL);
                    }
                };

                ProgressManager.getInstance().runProcessWithProgressAsynchronously(backgroundable, indicator);
            }
        };
    }
}
