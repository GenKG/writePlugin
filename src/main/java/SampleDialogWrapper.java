import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SampleDialogWrapper extends DialogWrapper {
    private String text;

    public SampleDialogWrapper(String text) {
        super(true);
        this.text = text;
        setModal(true);
        setCrossClosesWindow(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final var dialogPanel = new JPanel(new BorderLayout());
        final var label = new JLabel(this.text);
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }
}