import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Comment Lint";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        final var settings = AppSettingsState.getInstance();
        final var modified = !mySettingsComponent.getUserNameText().equals(settings.glvrdAPIKey) ||
                !mySettingsComponent.getDemoSelected().equals(settings.isDemo);

        return modified;
    }

    @Override
    public void apply() {
        final var settings = AppSettingsState.getInstance();

        settings.isDemo = mySettingsComponent.getDemoSelected();

        if (mySettingsComponent.getDemoSelected()) {
            new SampleDialogWrapper("Применение настроек будет после перезагрузки IDE").show();
            return;
        }

        final var apiKey = mySettingsComponent.getUserNameText();
        HTTP_API httpAPI = new HTTP_API(apiKey);
        try {
            var glvrdStatus = httpAPI.status();
            if (glvrdStatus.period_underlimit) {
                new SampleDialogWrapper("Аккаунт активен. Применение настроек будет после перезагрузки IDE").show();
            } else {
                new SampleDialogWrapper("Исчерпан лимит запросов. Применение настроек будет после перезагрузки IDE").show();
            }
            settings.glvrdAPIKey = apiKey;
        } catch (Exception e) {
            new SampleDialogWrapper(e.getMessage()).show();
        }
    }

    @Override
    public void reset() {
        final var settings = AppSettingsState.getInstance();
        mySettingsComponent.setUserNameText(settings.glvrdAPIKey);
        mySettingsComponent.setDemoCheckbox(settings.isDemo);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}