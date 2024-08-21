package me.lotabout.codegenerator.config.include;

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
public class IncludeList {
    @XmlElement(type = Include.class)
    @XmlElementWrapper
    private List<Include> includes = new ArrayList<>();

    public IncludeList() {
    }

    public IncludeList(final List<Include> includes) {
        this.includes.addAll(includes);
    }

    public IncludeList(final Include template) {
        this.includes.add(template);
    }

    public List<Include> getIncludes() {
        includes.forEach(Include::regenerateId);
        return includes;
    }

    public void setIncludes(final List<Include> includes) {
        this.includes = includes;
    }

    public static List<Include> fromXML(final String xml) {
        final var list = JAXB.unmarshal(new StringReader(xml), IncludeList.class);
        return list.getIncludes();
    }

    public static String toXML(final List<Include> templates) {
        final var templateList = new IncludeList(templates);
        final var sw = new StringWriter();
        JAXB.marshal(templateList, sw);
        return sw.toString();
    }

    public static String toXML(final Include templates) {
        final var templateList = new IncludeList(templates);
        final var sw = new StringWriter();
        JAXB.marshal(templateList, sw);
        return sw.toString();
    }
}
