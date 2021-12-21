import com.intellij.codeInspection.*;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;

import java.util.*;

class ProblemInfo {
    public String descriptionTemplate;
    public TextRange textRange;

    ProblemInfo(final String descriptionTemplate, final TextRange textRange) {
        this.descriptionTemplate = descriptionTemplate;
        this.textRange = textRange;
    }
}

public final class MySpellChecking extends LocalInspectionTool {
    private static final GLVRD glvrd = new GLVRD(AppSettingsState.getInstance().getState().glvrdAPIKey);
    private static final HashMap<String, ArrayList<ProblemInfo>> hashMapCommentText = new HashMap<String, ArrayList<ProblemInfo>>();

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
        final ProgressIndicator original = ProgressManager.getInstance().getProgressIndicator();
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
                final String elementText = psiComment.getText();
                final String elementKey = elementText.trim();
                if (elementKey.length() < 5) {
                    return;
                }

                Task.Backgroundable backgroundable = new Task.Backgroundable(psiComment.getProject(), elementKey) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        if (hashMapCommentText.containsKey(elementKey)) {
                            var problems = hashMapCommentText.get(elementKey);
                            if (problems.isEmpty()) {
//                                NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
//                                        .createNotification("Объект пуст и взят из кэша", NotificationType.INFORMATION)
//                                        .notify(psiComment.getProject());
                                return;
                            }
                            for (var problem : problems) {
                                if (problem != null) {
                                    ProblemDescriptorBase problemDescriptor = new ProblemDescriptorBase(psiComment, psiComment, problem.descriptionTemplate, LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, problem.textRange, false, isOnTheFly);
                                    holder.registerProblem(problemDescriptor);
                                }
                            }
//                            NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
//                                    .createNotification("Объект взят из кэша", NotificationType.INFORMATION)
//                                    .notify(psiComment.getProject());
                            return;
                        }

                        GlvrdResponse map = null;
                        try {
                            map = glvrd.proofRead(elementText);

                            ArrayList<ProblemInfo> problems = new ArrayList<ProblemInfo>();
                            if (!map.fragments.isEmpty()) {
                                for (Fragment glvrdFragment : map.fragments) {
                                    TextRange textRange = new TextRange(glvrdFragment.start, glvrdFragment.end);

                                    final var hintText = glvrd.hints(glvrdFragment.hint_id);
                                    final var hintData = hintText.hints.get(glvrdFragment.hint_id);
                                    String desc = String.format("GLVRD: %s", hintData.get("name").asText());

                                    ProblemDescriptorBase problemDescriptor = new ProblemDescriptorBase(psiComment, psiComment, desc, LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, textRange, false, isOnTheFly);
                                    holder.registerProblem(problemDescriptor);

                                    ProblemInfo problemInfo = new ProblemInfo(problemDescriptor.getDescriptionTemplate(), textRange);
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
                            if (finalMap != null) {
                                NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                        .createNotification("Result code: " + elementKey + " " + finalMap.score, NotificationType.INFORMATION)
                                        .notify(psiComment.getProject());
                            }
                        };

                        ApplicationManager.getApplication().invokeLater(onEnd);
                    }
                };

                ProgressManager.getInstance().runProcessWithProgressAsynchronously(backgroundable, indicator);
            }
        };
    }
}
