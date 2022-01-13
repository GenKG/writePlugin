import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SampleDialogWrapper extends DialogWrapper {

    public SampleDialogWrapper() {
        super(true);
        setModal(true);
        setCrossClosesWindow(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final var dialogPanel = new JPanel(new BorderLayout());
        final var label = new JLabel("Перезагрузите IDE для применения изменений");
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }
}