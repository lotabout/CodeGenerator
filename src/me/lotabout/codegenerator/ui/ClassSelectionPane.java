package me.lotabout.codegenerator.ui;

import com.intellij.openapi.ui.Messages;
import me.lotabout.codegenerator.config.ClassSelectionConfig;

import javax.swing.*;

public class ClassSelectionPane implements PipelineStepConfig {
    private JPanel topPane;
    private JTextField initialClassText;
    private JButton removeStepButton;
    private JTextField stepNumberText;

    public ClassSelectionPane(ClassSelectionConfig config, TemplateEditPane parent) {
        initialClassText.setText(config.initialClass);
        stepNumberText.setText(String.valueOf(config.stepNumber));

        removeStepButton.addActionListener(e -> {
            int result = Messages.showYesNoDialog("Really remove this step?", "Delete", null);
            if (result == Messages.OK) {
                parent.removePipelineStep(this);
            }
        });
    }

    @Override
    public int step() {
        return Integer.valueOf(stepNumberText.getText());
    }

    @Override
    public ClassSelectionConfig getConfig() {
        ClassSelectionConfig config = new ClassSelectionConfig();
        config.initialClass = initialClassText.getText();
        config.stepNumber = this.step();
        return config;
    }

    @Override
    public JPanel getComponent() {
        return topPane;
    }
}
