import com.intellij.codeInspection.*;
import com.intellij.lang.*;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MySpellChecking extends LocalInspectionTool {
    private static final GLVRD glvrd = new GLVRD("KEY");

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
                if (holder.getResultCount() > 1000) return;

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

                String elementText = element.getText();

                if (elementText.length() > 5 && elementText.startsWith("//") && elementType.toString().equals("END_OF_LINE_COMMENT")) {
                    try {
                        String result = glvrd.glvrdProofRead(elementText);
                        TextRange textRange = new TextRange(0, 7); // todo исправить text range: брать из ответа главреда API

                        ProblemDescriptorBase problemDescriptor = new ProblemDescriptorBase(element, element, "Коммент не очень. Исправьте: " + result, null, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, textRange, true, isOnTheFly);
                        holder.registerProblem(problemDescriptor);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
    }
}
