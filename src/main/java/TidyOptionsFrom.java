import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

public class TidyOptionsFrom implements ItemListener {
    private JPanel contentPane;
    private JPanel localPanel;

    Map<String, OptionComponent> optionComponents;

    public JPanel getContentPane() {
        return contentPane;
    }

    private static class OptionComponent {
        public JComponent textField;

        public OptionComponent(JComponent textFieldComponent) {
            this.textField = textFieldComponent;
        }
    }

    public TidyOptionsFrom() {
        optionComponents = new HashMap<>();
        localPanel.setLayout(new BoxLayout(localPanel, BoxLayout.Y_AXIS));
        JPanel optionPanel = localPanel;
        JPanel optionRow = new JPanel();
        localPanel.add(new JLabel("Local namespace"));

        optionRow.setLayout(new BoxLayout(optionRow, BoxLayout.X_AXIS));
        optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField textField = new JTextField();
        textField.setToolTipText("Use github.com/namespace");

        optionRow.add(textField);

        optionPanel.add(optionRow);
        OptionComponent optionComponent = new OptionComponent(textField);
        optionComponents.put("localPrefix", optionComponent);
    }

    public String getOptionText(String optionId) {
        String text = null;
        JTextField textField = (JTextField) optionComponents.get(optionId).textField;
        if (textField != null) {
            text = textField.getText();
        }

        return text;
    }

    public void setOptionText(String optionId, String text){
        ((JTextField) optionComponents.get(optionId).textField).setText(text);
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

    }
}
