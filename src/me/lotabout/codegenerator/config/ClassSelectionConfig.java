package me.lotabout.codegenerator.config;

public class ClassSelectionConfig implements PipelineStep {
    public String initialClass = "$class0.name";
    public int stepNumber = 1;
    public boolean enabled;
    @Override public String type() {
        return "class-selection";
    }

    @Override
    public int step() {
        return stepNumber;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassSelectionConfig that = (ClassSelectionConfig) o;

        if (stepNumber != that.stepNumber) return false;
        if (enabled != that.enabled) return false;
        return initialClass != null ? initialClass.equals(that.initialClass) : that.initialClass == null;
    }

    @Override
    public int hashCode() {
        int result = initialClass != null ? initialClass.hashCode() : 0;
        result = 31 * result + stepNumber;
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
