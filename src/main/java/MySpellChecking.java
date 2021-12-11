import com.intellij.codeInspection.*;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private static final GLVRD glvrd = new GLVRD("KEY");
    private static final HashMap<String, ArrayList<ProblemInfo>> hashMap = new HashMap<String, ArrayList<ProblemInfo>>();

    private static final Set<Integer> psiElementsSet = new HashSet<Integer>();

    @Override
    public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
        return super.getBatchSuppressActions(element);
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        return super.isSuppressedFor(element);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitComment(@NotNull final PsiComment element) {
                // cancelForSmallText
                final String elementText = element.getText();
                final String elementKey = elementText.trim();
                if (elementKey.length() < 5) {
                    return;
                }

                if (hashMap.containsKey(elementKey)) {
                    var problems = hashMap.get(elementKey);
                    if (problems.isEmpty()) {
                        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                .createNotification("Объект пуст и взят из кэша", NotificationType.INFORMATION)
                                .notify(element.getProject());
                        return;
                    }
                    for (var problem : problems) {
                        if (problem != null) {
                            ProblemDescriptorBase problemDescriptor = new ProblemDescriptorBase(element, element, problem.descriptionTemplate, LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, problem.textRange, true, isOnTheFly);
                            holder.registerProblem(problemDescriptor);
                        }
                    }
                    NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                            .createNotification("Объект взят из кэша", NotificationType.INFORMATION)
                            .notify(element.getProject());
                    return;
                }

                try {
                    final var map = glvrd.proofRead(elementText);
                    ArrayList<ProblemInfo> problems = new ArrayList<ProblemInfo>();

                    if (map.fragments.isEmpty()) {
                        hashMap.put(elementKey, problems);
                    } else {
                        for (Fragment glvrdFragment : map.fragments) {
                            TextRange textRange = new TextRange(glvrdFragment.start, glvrdFragment.end);
                            final var hintText = glvrd.hints(glvrdFragment.hint_id);
                            final var hintData = hintText.hints.get(glvrdFragment.hint_id);

                            String desc = String.format("GLVRD: %s", hintData.get("name").asText());

                            ProblemDescriptorBase problemDescriptor = new ProblemDescriptorBase(element, element, desc, LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, textRange, true, isOnTheFly);
                            holder.registerProblem(problemDescriptor);

                            ProblemInfo problemInfo = new ProblemInfo(problemDescriptor.getDescriptionTemplate(), textRange);
                            problems.add(problemInfo);
                        }
                        hashMap.put(elementKey, problems);
                    }

                    NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                            .createNotification("Result code: " + map.score, NotificationType.INFORMATION)
                            .notify(element.getProject());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
