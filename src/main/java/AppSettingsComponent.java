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
    private final JButton bitcoinLabel = new BrowserLink("bitcoin:bc1qejh37h2epmkrs0vmrv480fc27e0z4arkncevcp");
    private final JCheckBox demoCheckbox = new JCheckBox();

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Использовать демо режим"), demoCheckbox)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Приобрести ключ"), glvrdAPILink, 1, false)
                .addLabeledComponent(new JBLabel("Введите ключ для активации"), glvrdKeyText, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .addLabeledComponent(new JBLabel("🍺 Поддержи разработку! Отправь BTC на кошелк: "), bitcoinLabel)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return glvrdKeyText;
    }

    public void setDemoCheckbox(@NotNull boolean value) {
        demoCheckbox.setSelected(value);
    }

    public Boolean getDemoSelected() {
        return demoCheckbox.isSelected();
    }

    @NotNull
    public String getHTTPAPIText() {
        return glvrdKeyText.getText();
    }

    public void setHTTPAPIText(@NotNull String newText) {
        glvrdKeyText.setText(newText);
    }
}