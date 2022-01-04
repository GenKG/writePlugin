import com.intellij.ui.components.BrowserLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField glvrdKeyText = new JBTextField();
    private final JButton glvrdAPILink = new BrowserLink("glvrd.ru", "https://glvrd.ru/api/");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Ключ для запросов: "), glvrdKeyText, 1, false)
                .addLabeledComponent(new JBLabel("Powered by"), glvrdAPILink, 2, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return glvrdKeyText;
    }

    @NotNull
    public String getUserNameText() {
        return glvrdKeyText.getText();
    }

    public void setUserNameText(@NotNull String newText) {
        glvrdKeyText.setText(newText);
    }
}