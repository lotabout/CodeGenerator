package me.lotabout.codegenerator.config;

public class ClassSelectionConfig implements PipelineStep {
    public String initialClass = "$class0.name";
    public boolean enabled;
    public String postfix = "";
    @Override public String type() {
        return "class-selection";
    }

    @Override
    public String postfix() {
        return postfix;
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

        if (enabled != that.enabled) return false;
        if (initialClass != null ? !initialClass.equals(that.initialClass) : that.initialClass != null) return false;
        return postfix != null ? postfix.equals(that.postfix) : that.postfix == null;
    }

    @Override
    public int hashCode() {
        int result = initialClass != null ? initialClass.hashCode() : 0;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (postfix != null ? postfix.hashCode() : 0);
        return result;
    }
}
