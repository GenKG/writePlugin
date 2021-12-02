import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.net.*;

public class HelloWorldClass extends AnAction {

    private String glvrdProofRead(String text, String key) throws Exception {
        URL url = new URL("https://glvrd.ru/api/v3/proofread/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("X-GLVRD-KEY", key);
        con.setUseCaches(true);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");

        String urlParameters = "text=" + text;
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        writer.write(urlParameters);
        writer.close();

        int status = con.getResponseCode();
        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                return sb.toString();
        }
        return "";
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        // Get required data keys
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        JBPopupFactory factory = JBPopupFactory.getInstance();
        String selectionText = editor.getCaretModel().getCurrentCaret().getSelectedText();
//        factory.createMessage(editor.getCaretModel().getCurrentCaret().getSelectedText().toUpperCase(Locale.ROOT)).showInFocusCenter();

        try {
            String result = this.glvrdProofRead(selectionText, "KEY");
            NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group")
                    .createNotification(result, NotificationType.INFORMATION)
                    .notify(project);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
