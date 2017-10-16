package me.lotabout.codegenerator.config;

public class ClassSelectionConfig implements PipelineStep {
    public String initialClass = "";
    @Override public String type() {
        return "class-selection";
    }
}
