package me.lotabout.codegenerator;

public class CodeTemplate {
    public static final String DEFAULT_ENCODING = "UTF-8";

    private String type = "";
    private String name = "";
    private String template = "";
    private String fileEncoding = DEFAULT_ENCODING;

    public CodeTemplate setType(String type) {
        this.type = type;
        return this;
    }

    public CodeTemplate setName(String name) {
        this.name = name;
        return this;
    }

    public CodeTemplate setTemplate(String template) {
        this.template = template;
        return this;
    }

    public CodeTemplate setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
        return this;
    }

    public String type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String template() {
        return template;
    }

    public String fileEncoding() {
        return fileEncoding;
    }
}
