package me.lotabout.codegenerator.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class CodeTemplateList {
    @XmlElement(type = CodeTemplate.class)
    @XmlElementWrapper
    private List<CodeTemplate> templates = new ArrayList<>();

    public CodeTemplateList() {}

    public CodeTemplateList(final List<CodeTemplate> templates) {
        this.templates.addAll(templates);
    }

    public CodeTemplateList(final CodeTemplate template) {
        this.templates.add(template);
    }

    public List<CodeTemplate> getTemplates() {
        templates.forEach(CodeTemplate::regenerateId);
        return templates;
    }

    public void setTemplates(final List<CodeTemplate> templates) {
        this.templates = templates;
    }

    public static List<CodeTemplate> fromXML(final String xml) {
        final CodeTemplateList list = JAXB.unmarshal(new StringReader(xml), CodeTemplateList.class);
        return list.getTemplates();
    }

    public static String toXML(final List<CodeTemplate> templates) {
        final CodeTemplateList templateList = new CodeTemplateList(templates);
        final StringWriter sw = new StringWriter();
        JAXB.marshal(templateList, sw);
        return sw.toString();
    }
    public static String toXML(final CodeTemplate templates) {
        final CodeTemplateList templateList = new CodeTemplateList(templates);
        final StringWriter sw = new StringWriter();
        JAXB.marshal(templateList, sw);
        return sw.toString();
    }
}
