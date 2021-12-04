package eu.oakroot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

public class TidyImportsOptionsForm implements ItemListener {
    public static final String LOCAL_PREFIX = "localPrefix";
    public static final String FORMAT_ON_SAVE = "formatOnSave";

    private JPanel contentPanel;
    private JPanel localPanel;
    private JPanel experimentalPanel;

    private static class OptionComponent {
        public JComponent component;

        public OptionComponent(JComponent fieldComponent) {
            this.component = fieldComponent;
        }
    }

    Map<String, OptionComponent> optionComponents;

    public TidyImportsOptionsForm() {
        optionComponents = new HashMap<>();

        JPanel optionRow = new JPanel();
        JPanel optionPanel = localPanel;
        optionPanel.setLayout(new BoxLayout(localPanel, BoxLayout.Y_AXIS));
        optionPanel.add(new JLabel("Local namespace"));
        optionRow.setLayout(new BoxLayout(optionRow, BoxLayout.X_AXIS));
        optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField textField = new JTextField();
        textField.setToolTipText("Use github.com/namespace");
        optionRow.add(textField);
        optionPanel.add(optionRow);
        optionComponents.put(LOCAL_PREFIX, new TidyImportsOptionsForm.OptionComponent(textField));

        optionRow = new JPanel();
        optionPanel = experimentalPanel;
        optionPanel.setLayout(new BoxLayout(experimentalPanel, BoxLayout.Y_AXIS));
        optionPanel.add(new JLabel("Experimental"));
        optionRow.setLayout(new BoxLayout(optionRow, BoxLayout.X_AXIS));
        optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JCheckBox checkBox = new JCheckBox();
        checkBox.setText("Format imports on save");
        optionRow.add(checkBox);
        optionPanel.add(optionRow);

        optionComponents.put(FORMAT_ON_SAVE, new TidyImportsOptionsForm.OptionComponent(checkBox));
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

    }

    public JPanel getContentPane() {
        return contentPanel;
    }

    public boolean isOptionActive(String optionId) {
        return ((JCheckBox) optionComponents.get(optionId).component).isSelected();
    }

    public void setOptionActive(String optionId, boolean selected){
        ((JCheckBox) optionComponents.get(optionId).component).setSelected(selected);
    }

    public String getOptionText(String optionId) {
        String text = null;
        JTextField textField = (JTextField) optionComponents.get(optionId).component;
        if (textField != null) {
            text = textField.getText();
        }

        return text;
    }

    public void setOptionText(String optionId, String text) {
        ((JTextField) optionComponents.get(optionId).component).setText(text);
    }
}
