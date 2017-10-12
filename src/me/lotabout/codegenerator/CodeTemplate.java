package me.lotabout.codegenerator;

public class CodeTemplate {
    public static final String DEFAULT_ENCODING = "UTF-8";

    private String name = "";
    private String type = "";
    private String template = "";
    private String fileEncoding = DEFAULT_ENCODING;

    public CodeTemplate setName(String name) {
        this.name = name;
        return this;
    }

    public CodeTemplate setType(String type) {
        this.type = type;
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

    public boolean isValid() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeTemplate that = (CodeTemplate) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (template != null ? !template.equals(that.template) : that.template != null) return false;
        return fileEncoding != null ? fileEncoding.equals(that.fileEncoding) : that.fileEncoding == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (template != null ? template.hashCode() : 0);
        result = 31 * result + (fileEncoding != null ? fileEncoding.hashCode() : 0);
        return result;
    }
}
