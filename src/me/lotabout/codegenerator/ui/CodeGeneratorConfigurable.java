package me.lotabout.codegenerator.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CodeGeneratorConfigurable implements SearchableConfigurable {
    private CodeGeneratorSettings settings;
    private CodeGeneratorConfig config;

    public CodeGeneratorConfigurable() {
        this.settings = ServiceManager.getService(CodeGeneratorSettings.class);
    }

    @NotNull @Override public String getId() {
        return "plugins.codegenerator";
    }

    @Nls @Override public String getDisplayName() {
        return "CodeGenerator";
    }

    @Nullable @Override public JComponent createComponent() {
        if (config == null) {
            config = new CodeGeneratorConfig(settings);
        }
        return config.getMainPane();
    }

    @Override public boolean isModified() {
        Map<String, CodeTemplate> codeTemplateList = config.getTabTemplates();
        if (settings.getCodeTemplates().size() != config.getTabTemplates().size()) {
            return true;
        }

        for (Map.Entry<String, CodeTemplate> entry : config.getTabTemplates().entrySet()) {
            Optional<CodeTemplate> codeTemplate = settings.getCodeTemplate(entry.getKey());
            if (!codeTemplate.isPresent() || !codeTemplate.get().equals(entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    @Override public void apply() throws ConfigurationException {
        for (Map.Entry<String, CodeTemplate> entry : config.getTabTemplates().entrySet()) {
            if (!entry.getValue().isValid()) {
                throw new ConfigurationException(
                        "Not property can be empty and classNumber should be a number");
            }
        }
        settings.setCodeTemplates(config.getTabTemplates());
        config.refresh(settings);
    }
}
