package me.lotabout.codegenerator.config;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class CodeTemplateList {
    @XmlElement(type = CodeTemplate.class)
    @XmlElementWrapper
    private List<CodeTemplate> templates = new ArrayList<>();

    public CodeTemplateList() {}
    public CodeTemplateList(List<CodeTemplate> templates) {
        this.templates.addAll(templates);
    }
    public CodeTemplateList(CodeTemplate template) {
        this.templates.add(template);
    }

    public List<CodeTemplate> getTemplates() {
        templates.forEach(CodeTemplate::regenerateId);
        return templates;
    }

    public void setTemplates(List<CodeTemplate> templates) {
        this.templates = templates;
    }

    public static List<CodeTemplate> fromXML(String xml) {
        CodeTemplateList list = JAXB.unmarshal(new StringReader(xml), CodeTemplateList.class);
        return list.getTemplates();
    }

    public static String toXML(List<CodeTemplate> templates) {
        CodeTemplateList templateList = new CodeTemplateList(templates);
        StringWriter sw = new StringWriter();
        JAXB.marshal(templateList, sw);
        return sw.toString();
    }
    public static String toXML(CodeTemplate templates) {
        CodeTemplateList templateList = new CodeTemplateList(templates);
        StringWriter sw = new StringWriter();
        JAXB.marshal(templateList, sw);
        return sw.toString();
    }
}

