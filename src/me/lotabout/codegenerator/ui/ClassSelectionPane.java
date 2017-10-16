package me.lotabout.codegenerator.ui;

import com.intellij.openapi.ui.Messages;
import me.lotabout.codegenerator.config.ClassSelectionConfig;

import javax.swing.*;

public class ClassSelectionPane implements PipelineStepConfig {
    private JPanel topPane;
    private JTextField initialClassText;
    private JButton removeStepButton;

    public ClassSelectionPane(ClassSelectionConfig config, TemplateEditPane parent) {
        initialClassText.setText(config.initialClass);

        removeStepButton.addActionListener(e -> {
            int result = Messages.showYesNoDialog("Really remove this step?", "Delete", null);
            if (result == Messages.OK) {
                parent.removePipelineStep(this);
            }
        });
    }

    @Override
    public ClassSelectionConfig getConfig() {
        ClassSelectionConfig config = new ClassSelectionConfig();
        config.initialClass = initialClassText.getText();
        return config;
    }

    @Override
    public JPanel getComponent() {
        return topPane;
    }
}
