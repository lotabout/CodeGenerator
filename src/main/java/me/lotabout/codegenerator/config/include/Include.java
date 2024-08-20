package me.lotabout.codegenerator.config.include;

import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.util.UUID;

@XmlRootElement(name = "include")
@XmlAccessorType(XmlAccessType.FIELD)
public class Include {
    @XmlAttribute
    public static final String VERSION = "1.3";

    private UUID id;
    public String name = "Untitled";
    public String content = DEFAULT_TEMPLATE;
    public boolean defaultInclude;

    public Include(UUID id) {
        this.id = id;
    }

    public Include(String id) {
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDefaultInclude() {
        return defaultInclude;
    }

    public void setDefaultInclude(boolean defaultInclude) {
        this.defaultInclude = defaultInclude;
    }

    private static final String DEFAULT_TEMPLATE;

    static {
        String default_template;
        try {
            default_template = FileUtil.loadTextAndClose(Include.class.getResourceAsStream("/template/default-include.vm"));
        } catch (IOException e) {
            default_template = "";
            e.printStackTrace();
        }
        DEFAULT_TEMPLATE = default_template;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        var include1 = (Include) o;

        return new EqualsBuilder().append(id, include1.id)//
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
