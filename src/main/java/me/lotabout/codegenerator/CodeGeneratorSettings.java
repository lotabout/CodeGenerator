package me.lotabout.codegenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;

import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.CodeTemplateList;
import me.lotabout.codegenerator.config.include.Include;

@State(name = "CodeGeneratorSettings", storages = {@Storage("$APP_CONFIG$/CodeGenerator-settings.xml")})
public class CodeGeneratorSettings implements PersistentStateComponent<CodeGeneratorSettings> {

    private static final Logger LOGGER = Logger.getInstance(CodeGeneratorSettings.class);
    private List<CodeTemplate> codeTemplates;
    private List<Include> includes;

    public CodeGeneratorSettings() {

    }

    public List<Include> getIncludes() {
        if (includes == null) {
            includes = new ArrayList<>();
        }
        return includes;
    }

    public void setIncludes(final List<Include> includes) {
        this.includes = includes;
    }

    public CodeGeneratorSettings setCodeTemplates(final List<CodeTemplate> codeTemplates) {
        this.codeTemplates = codeTemplates;
        return this;
    }


    @Nullable
    @Override
    public CodeGeneratorSettings getState() {
        if (codeTemplates == null) {
            codeTemplates = loadDefaultTemplates();
        }
        return this;
    }

    @Override
    public void loadState(final CodeGeneratorSettings codeGeneratorSettings) {
        XmlSerializerUtil.copyBean(codeGeneratorSettings, this);
        
        // 修复可能的null字段问题
        if (this.codeTemplates != null) {
            for (CodeTemplate template : this.codeTemplates) {
                if (template != null) {
                    // 确保这些字段不为null，避免NPE
                    if (template.whenDuplicatesOption == null) {
                        template.whenDuplicatesOption = org.jetbrains.java.generate.config.DuplicationPolicy.ASK;
                    }
                    if (template.insertNewMethodOption == null) {
                        template.insertNewMethodOption = org.jetbrains.java.generate.config.InsertWhere.AT_CARET;
                    }
                }
            }
        }
    }

    public List<CodeTemplate> getCodeTemplates() {
        if (codeTemplates == null) {
            codeTemplates = loadDefaultTemplates();
        }
        
        // 确保所有模板的字段不为null
        for (CodeTemplate template : codeTemplates) {
            if (template != null) {
                if (template.whenDuplicatesOption == null) {
                    template.whenDuplicatesOption = org.jetbrains.java.generate.config.DuplicationPolicy.ASK;
                }
                if (template.insertNewMethodOption == null) {
                    template.insertNewMethodOption = org.jetbrains.java.generate.config.InsertWhere.AT_CARET;
                }
            }
        }
        
        return codeTemplates;
    }

    public Optional<CodeTemplate> getCodeTemplate(final String templateId) {
        Optional<CodeTemplate> template = codeTemplates.stream()
                .filter(t -> t != null && t.getId().equals(templateId))
                .findFirst();
        
        // 确保返回的模板字段不为null
        template.ifPresent(t -> {
            if (t.whenDuplicatesOption == null) {
                t.whenDuplicatesOption = org.jetbrains.java.generate.config.DuplicationPolicy.ASK;
            }
            if (t.insertNewMethodOption == null) {
                t.insertNewMethodOption = org.jetbrains.java.generate.config.InsertWhere.AT_CARET;
            }
        });
        
        return template;
    }

    public Optional<Include> getInclude(final String includeId) {
        return includes.stream()
                .filter(t -> t != null && t.getId().equals(includeId))
                .findFirst();
    }

    public void removeCodeTemplate(final String templateId) {
        codeTemplates.removeIf(template -> template.name.equals(templateId));
    }

    private List<CodeTemplate> loadDefaultTemplates() {
        final List<CodeTemplate> templates = new ArrayList<>();
        try {
            templates.addAll(loadTemplates("getters-and-setters.xml"));
            templates.addAll(loadTemplates("to-string.xml"));
            templates.addAll(loadTemplates("HUE-Serialization.xml"));
        } catch (final Exception e) {
            LOGGER.error("loadDefaultTemplates failed", e);
        }
        return templates;
    }

    private List<CodeTemplate> loadTemplates(final String templateFileName) throws IOException {
        final InputStream in = CodeGeneratorSettings.class.getResourceAsStream("/template/" + templateFileName);
        if (in == null) {
            throw new IOException("Resource not found: " + templateFileName);
        }
        return CodeTemplateList.fromXML(FileUtil.loadTextAndClose(in));
    }
}
