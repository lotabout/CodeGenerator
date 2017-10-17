package me.lotabout.codegenerator.ui;

import com.intellij.openapi.ui.Messages;
import me.lotabout.codegenerator.config.ClassSelectionConfig;

import javax.swing.*;

public class ClassSelectionPane implements PipelineStepConfig {
    private JPanel topPane;
    private JTextField initialClassText;
    private JButton removeStepButton;
    private JTextField stepPostfixText;
    private JCheckBox enableStepCheckBox;

    public ClassSelectionPane(ClassSelectionConfig config, TemplateEditPane parent) {
        initialClassText.setText(config.initialClass);
        stepPostfixText.setText(config.postfix());
        enableStepCheckBox.setSelected(config.enabled());

        removeStepButton.addActionListener(e -> {
            int result = Messages.showYesNoDialog("Really remove this step?", "Delete", null);
            if (result == Messages.OK) {
                parent.removePipelineStep(this);
            }
        });
    }

    @Override
    public String postfix() {
        return stepPostfixText.getText();
    }

    @Override
    public ClassSelectionConfig getConfig() {
        ClassSelectionConfig config = new ClassSelectionConfig();
        config.initialClass = initialClassText.getText();
        config.postfix = this.postfix();
        config.enabled = enableStepCheckBox.isSelected();
        return config;
    }

    @Override
    public JPanel getComponent() {
        return topPane;
    }
}
