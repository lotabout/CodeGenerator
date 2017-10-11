package me.lotabout.codegenerator;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@State(name = "CodeGeneratorSettings", storages = {@Storage(id = "app-default", file = "$APP_CONFIG$/CodeGenerator-settings.xml")})
public class CodeGeneratorSettings implements PersistentStateComponent<CodeGeneratorSettings> {

    private static final Logger LOGGER = Logger.getInstance(CodeGeneratorSettings.class);
    private Map<String, CodeTemplate> codeTemplates;

    public CodeGeneratorSettings() {

    }

    public CodeGeneratorSettings setCodeTemplates(
            Map<String, CodeTemplate> codeTemplates) {
        this.codeTemplates = codeTemplates;
        return this;
    }


    @Nullable @Override public CodeGeneratorSettings getState() {
        if (codeTemplates == null) {
            codeTemplates = loadDefaultTemplates();
        }
        return this;
    }

    @Override public void loadState(CodeGeneratorSettings codeGeneratorSettings) {
        XmlSerializerUtil.copyBean(codeGeneratorSettings, this);
    }

    public Map<String, CodeTemplate> getCodeTemplates() {
        if (codeTemplates == null) {
            codeTemplates = loadDefaultTemplates();
        }
        return codeTemplates;
    }

    public Optional<CodeTemplate> getCodeTemplate(String template) {
        return Optional.of(codeTemplates.get(template));
    }

    public void removeCodeTemplate(String template) {
        codeTemplates.remove(template);
    }


    private Map<String, CodeTemplate> loadDefaultTemplates() {
        try {
            Map<String, CodeTemplate> codeTemplates = new HashMap<>();
            codeTemplates.put("HUESerialization", createTemplate("HUESerialization.vm", "body", CodeTemplate.DEFAULT_ENCODING));
            return codeTemplates;
        } catch (Exception e) {
            LOGGER.error("loadDefaultTemplates failed", e);
        }
        return Collections.emptyMap();
    }

    @NotNull
    private CodeTemplate createTemplate(String sourceFileName, String type, String encoding) throws IOException {
        String velocityTemplate = FileUtil.loadTextAndClose(CodeGeneratorSettings.class.getResourceAsStream("/template/" + sourceFileName));
        return new CodeTemplate()
                .setType(type)
                .setName(sourceFileName)
                .setTemplate(velocityTemplate);
    }

}
