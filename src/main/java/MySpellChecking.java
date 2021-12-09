import com.intellij.codeInspection.*;
import com.intellij.lang.*;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;

public final class MySpellChecking extends LocalInspectionTool {
    private static final GLVRD glvrd = new GLVRD("KEY");
    private static final HashMap<String, ProblemDescriptorBase> hashMap = new HashMap<String, ProblemDescriptorBase>();

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
            public void visitElement(@NotNull final PsiElement element) {
                if (holder.getResultCount() > 1000) {
                    return;
                }

                final ASTNode node = element.getNode();
                if (node == null) {
                    return;
                }

                // Extract parser definition from element
                final IElementType elementType = node.getElementType();

                PsiFile containingFile = element.getContainingFile();
                if (containingFile != null && Boolean.TRUE.equals(containingFile.getUserData(InjectedLanguageManager.FRANKENSTEIN_INJECTION))) {
                    return;
                }

                if (elementType.toString().equals("C_STYLE_COMMENT") || elementType.toString().equals("END_OF_LINE_COMMENT")) {
                    final String elementText = element.getText();
                    final String elementKey = elementText.trim();
                    if (elementKey.length() < 5) {
                        return;
                    }
                    if (hashMap.containsKey(elementKey)) {
                        var problem = hashMap.get(elementKey);
                        if (problem == null) {
                            NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                    .createNotification("Объект пуст и взят из кэша", NotificationType.INFORMATION)
                                    .notify(element.getProject());
                            return;
                        }
                        holder.registerProblem(problem);
                        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                .createNotification("Объект взят из кэша", NotificationType.INFORMATION)
                                .notify(element.getProject());
                        return;
                    }
                    try {
                        final var map = glvrd.proofRead(elementText);
                        if (map.fragments.isEmpty()) {
                            hashMap.put(elementKey, null);
                        }
                        for (Fragment glvrdFragment : map.fragments) {
                            TextRange textRange = new TextRange(glvrdFragment.start, glvrdFragment.end);
                            final var hintText = glvrd.hints(glvrdFragment.hint_id);
                            final var hintData = hintText.hints.get(glvrdFragment.hint_id);

                            String desc = String.format("GLVRD: %s", hintData.get("name").asText());

                            ProblemDescriptorBase problemDescriptor = new ProblemDescriptorBase(element, element, desc, LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, textRange, true, isOnTheFly);
                            holder.registerProblem(problemDescriptor);
                            hashMap.put(elementKey, problemDescriptor);
                        }

                        NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                                .createNotification("Result code: " + map.score, NotificationType.INFORMATION)
                                .notify(element.getProject());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
    }
}
