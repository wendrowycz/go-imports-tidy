package eu.oakroot;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class TidyProjectSettingsConfigurable implements Configurable {
    Project project;
    TidyOptionsFrom tidyOptionsFrom;
    PropertiesComponent propertiesComponent;
    Map<Enum<TidyOptionsFactory.TYPE>, ArrayList<Map<String, String>>> tidyOptions;

    static TidyProjectSettingsConfigurable instance;

    public TidyProjectSettingsConfigurable(Project project) {
        propertiesComponent = PropertiesComponent.getInstance(project);
        this.project = project;
        instance = this;
    }

    static public TidyProjectSettingsConfigurable getInstance() {
        return instance;
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

    @Nullable
    @Override
    public JComponent createComponent() {
        tidyOptionsFrom = new TidyOptionsFrom();
        return tidyOptionsFrom.getContentPane();
    }

    public static String getOptionTextString(Project project, String optionId) {
        String retValue = PropertiesComponent.getInstance(project).getValue(optionId);
        if (retValue == null) {
            return "";
        }
        return retValue;
    }

    @Override
    public boolean isModified() {
        String optionId = "localPrefix";
        String textInForm = tidyOptionsFrom.getOptionText(optionId);
        String savedOptionText = propertiesComponent.getValue(optionId);
        return !textInForm.equals(savedOptionText);
    }

    @Override
    public void apply() {
        propertiesComponent.setValue("localPrefix", tidyOptionsFrom.getOptionText("localPrefix"));
    }

    @Override
    public void reset() {
        String optionId = "localPrefix";
        String savedOptionText = propertiesComponent.getValue(optionId);
        tidyOptionsFrom.setOptionText(optionId, Objects.requireNonNullElse(savedOptionText, ""));
    }

    @Override
    public void disposeUIResources() {
        tidyOptionsFrom = null;
    }
}
