package me.lotabout.codegenerator.config.include;

import java.io.IOException;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.intellij.openapi.util.io.FileUtil;

@XmlRootElement(name = "include")
@XmlAccessorType(XmlAccessType.FIELD)
public class Include {
    @XmlAttribute
    public static final String VERSION = "1.3";

    private UUID id;
    public String name = "Untitled";
    public String content = DEFAULT_TEMPLATE;
    public boolean defaultInclude;

    public Include(final UUID id) {
        this.id = id;
    }

    public Include(final String id) {
        this.id = UUID.fromString(id);
    }

    public Include() {
        this(UUID.randomUUID());
    }

    public void regenerateId() {
        this.id = UUID.randomUUID();
    }

    public String getId() {
        return this.id.toString();
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public boolean isDefaultInclude() {
        return defaultInclude;
    }

    public void setDefaultInclude(final boolean defaultInclude) {
        this.defaultInclude = defaultInclude;
    }

    private static final String DEFAULT_TEMPLATE;

    static {
        String default_template;
        try {
            default_template = FileUtil.loadTextAndClose(Include.class.getResourceAsStream("/template/default-include.vm"));
        } catch (final IOException e) {
            default_template = "";
            e.printStackTrace();
        }
        DEFAULT_TEMPLATE = default_template;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var include1 = (Include) o;
        return new EqualsBuilder()
            .append(id, include1.id)//
            .append(name, include1.name)//
            .append(content, include1.content)//
            .append(defaultInclude, include1.defaultInclude)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)//
                .append(id)//
                .append(name)//
                .append(content)//
                .toHashCode();
    }
}
