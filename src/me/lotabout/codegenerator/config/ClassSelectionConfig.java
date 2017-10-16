package me.lotabout.codegenerator.config;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ClassSelectionConfig implements PipelineStep {
    public String initialClass = "$class0.name";
    @Override public String type() {
        return "class-selection";
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        ClassSelectionConfig that = (ClassSelectionConfig)o;

        return new EqualsBuilder()
                .append(initialClass, that.initialClass)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(initialClass)
                .toHashCode();
    }
}
