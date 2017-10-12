package me.lotabout.codegenerator;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import me.lotabout.codegenerator.config.GeneratorConfig;
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
    private Map<String, GeneratorConfig> codeTemplates;

    public CodeGeneratorSettings() {

    }

    public CodeGeneratorSettings setCodeTemplates(
            Map<String, GeneratorConfig> codeTemplates) {
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

    public Map<String, GeneratorConfig> getCodeTemplates() {
        if (codeTemplates == null) {
            codeTemplates = loadDefaultTemplates();
        }
        return codeTemplates;
    }

    public Optional<GeneratorConfig> getCodeTemplate(String template) {
        return Optional.ofNullable(codeTemplates.get(template));
    }

    public void removeCodeTemplate(String template) {
        codeTemplates.remove(template);
    }


    private Map<String, GeneratorConfig> loadDefaultTemplates() {
        try {
            Map<String, GeneratorConfig> codeTemplates = new HashMap<>();
            codeTemplates.put("HUESerialization", createTemplate("HUESerialization", "body"));
            return codeTemplates;
        } catch (Exception e) {
            LOGGER.error("loadDefaultTemplates failed", e);
        }
        return Collections.emptyMap();
    }

    @NotNull
    private GeneratorConfig createTemplate(String name, String type) throws IOException {
        String velocityTemplate = FileUtil.loadTextAndClose(CodeGeneratorSettings.class.getResourceAsStream("/template/" + name + ".vm"));
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.type = type;
        generatorConfig.name = name;
        generatorConfig.template = velocityTemplate;
        return generatorConfig;
    }

}
