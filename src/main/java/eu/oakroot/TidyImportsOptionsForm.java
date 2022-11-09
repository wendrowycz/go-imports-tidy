package eu.oakroot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

public class TidyImportsOptionsForm implements ItemListener {
    public static final String LOCAL_PREFIX = "localPrefix";

    public JPanel contentPanel;
    private JPanel localPanel;

    private static class OptionComponent {
        public JComponent component;

        public OptionComponent(JComponent fieldComponent) {
            this.component = fieldComponent;
        }
    }

    Map<String, OptionComponent> optionComponents;

    public TidyImportsOptionsForm() {
        optionComponents = new HashMap<>();

        JPanel optionPanel = localPanel;

        optionPanel.setLayout(new BoxLayout(localPanel, BoxLayout.Y_AXIS));
        optionPanel.add(new JLabel("Local namespace"));

        JPanel optionRow = getRow();

        JTextField textField = new JTextField();
        textField.setToolTipText("Use github.com/namespace");
        optionRow.add(textField);
        optionPanel.add(optionRow);
        optionComponents.put(LOCAL_PREFIX, new OptionComponent(textField));
    }

    private JPanel getRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {}

    public JPanel getContentPane() {
        return contentPanel;
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
