import com.intellij.openapi.util.TextRange;

class ProblemInfo {
    public String descriptionTemplate;
    public TextRange textRange;

    ProblemInfo(final String descriptionTemplate, final TextRange textRange) {
        this.descriptionTemplate = descriptionTemplate;
        this.textRange = textRange;
    }
}