package me.lotabout.codegenerator.ui;

import me.lotabout.codegenerator.config.PipelineStep;

import javax.swing.*;

public interface PipelineStepConfig {
    PipelineStep getConfig();
    JComponent getComponent();
}
