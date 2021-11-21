import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

public class HelloWorldClass extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        factory.createMessage("Hello, World!").showInFocusCenter();
    }
}
