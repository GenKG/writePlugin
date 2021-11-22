import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class HelloWorldClass extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
    @Override
    public void update(@NotNull final AnActionEvent e) {
      // Get required data keys
      final Project project = e.getProject();
      final Editor editor = e.getData(CommonDataKeys.EDITOR);
      JBPopupFactory factory = JBPopupFactory.getInstance();
      factory.createMessage(editor.getCaretModel().getCurrentCaret().getSelectedText().toUpperCase(Locale.ROOT)).showInFocusCenter();
    }
}
