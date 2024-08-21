package me.lotabout.codegenerator.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "classSelection")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassSelectionConfig implements PipelineStep {

    public String initialClass = "$class0.qualifiedName";

    public boolean enabled = true;

    public String postfix = "";

    @Override
    public String type() {
        return "class-selection";
    }

    @Override
    public String postfix() {
        return postfix;
    }

    @Override
    public void postfix(final String postfix) {
        this.postfix = postfix;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void enabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClassSelectionConfig that = (ClassSelectionConfig) o;

        if (enabled != that.enabled) {
            return false;
        }
        if (!Objects.equals(initialClass, that.initialClass)) {
            return false;
        }
        return (postfix != null) ? postfix.equals(that.postfix) : (that.postfix == null);
    }

    @Override
    public int hashCode() {
        int result = initialClass != null ? initialClass.hashCode() : 0;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (postfix != null ? postfix.hashCode() : 0);
        return result;
    }
}
