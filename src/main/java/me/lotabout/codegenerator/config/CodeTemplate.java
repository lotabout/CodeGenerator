package me.lotabout.codegenerator.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.XCollection;

@XmlRootElement(name = "codeTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class CodeTemplate {
    @XmlAttribute
    public static final String VERSION = "1.3";

    private UUID id;
    public String name = "Untitled";
    // public String fileNamePattern = ".*\\.java$";
    public TemplateType type = TemplateType.BODY;
    public boolean enabled = true;
    public String template = DEFAULT_TEMPLATE;
    public String fileEncoding = DEFAULT_ENCODING;
    @XmlElements({
            @XmlElement(name="memberSelection", type=MemberSelectionConfig.class),
            @XmlElement(name="classSelection", type=ClassSelectionConfig.class)
    })
    @XmlElementWrapper
    @XCollection(elementTypes = {MemberSelectionConfig.class, ClassSelectionConfig.class})
    public List<PipelineStep> pipeline = new ArrayList<>();

    public InsertWhere insertNewMethodOption = InsertWhere.AT_CARET;
    public DuplicationPolicy whenDuplicatesOption = DuplicationPolicy.ASK;
    public boolean jumpToMethod = true; // jump cursor to toString method
    public String classNameVm = "${class0.qualifiedName}Test";
    public boolean alwaysPromptForPackage = false;
    public String defaultTargetPackage;
    public String defaultTargetModule;

    public CodeTemplate(final UUID id) {
        this.id = id;
    }
    public CodeTemplate(final String id) {
        this.id = UUID.fromString(id);
    }

    public CodeTemplate() {
        this(UUID.randomUUID());
    }

    public void regenerateId() {
        this.id = UUID.randomUUID();
    }

    public String getId() {
        return this.id.toString();
    }

    public boolean isValid() {
        return true;
    }

    public static final String DEFAULT_ENCODING = "UTF-8";

    private static final String DEFAULT_TEMPLATE;

    static {
        String default_template;
        try {
            final InputStream in = CodeTemplate.class.getResourceAsStream("/template/default.vm");
            if (in == null) {
               throw new IOException("Cannot find default template");
            }
            default_template = FileUtil.loadTextAndClose(in);
        } catch (final IOException e) {
            default_template = "";
            e.printStackTrace();
        }
        DEFAULT_TEMPLATE = default_template;
    }

    @Override public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final CodeTemplate template1 = (CodeTemplate)o;

        return new EqualsBuilder()
                .append(enabled, template1.enabled)
                .append(jumpToMethod, template1.jumpToMethod)
                .append(alwaysPromptForPackage, template1.alwaysPromptForPackage)
                .append(id, template1.id)
                .append(name, template1.name)
                // .append(fileNamePattern, template1.fileNamePattern)
                .append(type, template1.type)
                .append(template, template1.template)
                .append(fileEncoding, template1.fileEncoding)
                .append(pipeline, template1.pipeline)
                .append(insertNewMethodOption, template1.insertNewMethodOption)
                .append(whenDuplicatesOption, template1.whenDuplicatesOption)
                .append(classNameVm, template1.classNameVm)
                .append(defaultTargetPackage, template1.defaultTargetPackage)
                .append(defaultTargetModule, template1.defaultTargetModule)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                // .append(fileNamePattern)
                .append(type)
                .append(enabled)
                .append(template)
                .append(fileEncoding)
                .append(pipeline)
                .append(insertNewMethodOption)
                .append(whenDuplicatesOption)
                .append(jumpToMethod)
                .append(classNameVm)
                .append(alwaysPromptForPackage)
                .append(defaultTargetPackage)
                .append(defaultTargetModule)
                .toHashCode();
    }


}
