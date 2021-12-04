package eu.oakroot;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class TidyImportsSettingsConfigurable implements Configurable {
    Project project;
    TidyImportsOptionsForm tidyOptionsFrom;
    PropertiesComponent propertiesComponent;

    static TidyImportsSettingsConfigurable instance;

    public TidyImportsSettingsConfigurable(Project project) {
        this.project = project;
        propertiesComponent = PropertiesComponent.getInstance(project);
        instance = this;
    }

    @Override
    public String getDisplayName() {
        return "GO Tidy Imports";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public @Nullable JComponent createComponent() {
        tidyOptionsFrom = new TidyImportsOptionsForm();
        return tidyOptionsFrom.getContentPane();
    }

    @Override
    public boolean isModified() {
        boolean isOptionActiveInForm = tidyOptionsFrom.isOptionActive(TidyImportsOptionsForm.FORMAT_ON_SAVE);
        boolean savedOptionIsActive = propertiesComponent.getBoolean(TidyImportsOptionsForm.FORMAT_ON_SAVE);
        if (isOptionActiveInForm != savedOptionIsActive) {
            return true;
        }

        String textInForm = tidyOptionsFrom.getOptionText(TidyImportsOptionsForm.LOCAL_PREFIX);
        String savedOptionText = propertiesComponent.getValue(TidyImportsOptionsForm.LOCAL_PREFIX);
        return !textInForm.equals(savedOptionText);
    }

    @Override
    public void apply() throws ConfigurationException {
        propertiesComponent.setValue(TidyImportsOptionsForm.LOCAL_PREFIX, tidyOptionsFrom.getOptionText(TidyImportsOptionsForm.LOCAL_PREFIX));
        propertiesComponent.setValue(TidyImportsOptionsForm.FORMAT_ON_SAVE, tidyOptionsFrom.isOptionActive(TidyImportsOptionsForm.FORMAT_ON_SAVE));
    }

    @Override
    public void reset() {
        String optionId = TidyImportsOptionsForm.FORMAT_ON_SAVE;
        boolean savedOptionIsActive = propertiesComponent.getBoolean(optionId);
        tidyOptionsFrom.setOptionActive(optionId, savedOptionIsActive);

        optionId = TidyImportsOptionsForm.LOCAL_PREFIX;
        String savedOptionText = propertiesComponent.getValue(optionId);
        tidyOptionsFrom.setOptionText(optionId, Objects.requireNonNullElse(savedOptionText, ""));
    }

    @Override
    public void disposeUIResources() {
        tidyOptionsFrom = null;
    }

    public static boolean isOptionActive(Project project, String optionId){
        return PropertiesComponent.getInstance(project).getBoolean(optionId);
    }

    public static String getOptionTextString(Project project, String optionId) {
        String retValue = PropertiesComponent.getInstance(project).getValue(optionId);
        if (retValue == null) {
            return "";
        }
        return retValue;
    }
}
