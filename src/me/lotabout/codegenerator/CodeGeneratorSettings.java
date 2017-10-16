package me.lotabout.codegenerator;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.MemberSelectionConfig;
import me.lotabout.codegenerator.config.PipelineStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@State(name = "CodeGeneratorSettings", storages = {@Storage(id = "app-default", file = "$APP_CONFIG$/CodeGenerator-settings.xml")})
public class CodeGeneratorSettings implements PersistentStateComponent<CodeGeneratorSettings> {

    private static final Logger LOGGER = Logger.getInstance(CodeGeneratorSettings.class);
    private List<CodeTemplate> codeTemplates;

    public CodeGeneratorSettings() {

    }

    public CodeGeneratorSettings setCodeTemplates(List<CodeTemplate> codeTemplates) {
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

    public List<CodeTemplate> getCodeTemplates() {
        if (codeTemplates == null) {
            codeTemplates = loadDefaultTemplates();
        }
        return codeTemplates;
    }

    public Optional<CodeTemplate> getCodeTemplate(String templateId) {
        return codeTemplates.stream()
                .filter(t -> t.getId().equals(templateId))
                .findFirst();
    }

    public void removeCodeTemplate(String templateId) {
        codeTemplates.removeIf(template -> template.name.equals(templateId));
    }

    private List<CodeTemplate> loadDefaultTemplates() {
        List<CodeTemplate> templates = new ArrayList<>();
        try {
            templates.add(createTemplate("HUESerialization", "body", Collections.singletonList(new MemberSelectionConfig())));
        } catch (Exception e) {
            LOGGER.error("loadDefaultTemplates failed", e);
        }

        return templates;
    }

    @NotNull
    private CodeTemplate createTemplate(String name, String type, List<PipelineStep> pipeline) throws IOException {
        String velocityTemplate = FileUtil.loadTextAndClose(CodeGeneratorSettings.class.getResourceAsStream("/template/" + name + ".vm"));
        CodeTemplate codeTemplate = new CodeTemplate();
        codeTemplate.type = type;
        codeTemplate.name = name;
        codeTemplate.template = velocityTemplate;
        codeTemplate.pipeline.addAll(pipeline);
        return codeTemplate;
    }

}
