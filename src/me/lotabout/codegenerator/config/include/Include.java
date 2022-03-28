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
    public String fileNamePattern = ".*\\.java$";
    public String content = DEFAULT_TEMPLATE;
    public String fileEncoding = DEFAULT_ENCODING;

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

    public static final String DEFAULT_ENCODING = "UTF-8";

    private static final String DEFAULT_TEMPLATE;

    static {
        String default_template;
        try {
            default_template = FileUtil.loadTextAndClose(Include.class.getResourceAsStream("/template/default.vm"));
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

        Include template1 = (Include) o;

        return new EqualsBuilder().append(id, template1.id).append(name, template1.name).append(fileNamePattern, template1.fileNamePattern).append(content, template1.content).append(fileEncoding, template1.fileEncoding).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(name).append(fileNamePattern).append(content).append(fileEncoding).toHashCode();
    }


}
