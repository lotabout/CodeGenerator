package me.lotabout.codegenerator.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.ui.include.IncludeConfig;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CodeGeneratorConfigurable implements SearchableConfigurable {
    private CodeGeneratorSettings settings;
    private CodeGeneratorConfig codeGeneratorConfig;
    private IncludeConfig includeConfig;
    private MainPaneConfig mainPaneConfig;

    public CodeGeneratorConfigurable() {
        this.settings = ServiceManager.getService(CodeGeneratorSettings.class);
    }

    @NotNull
    @Override
    public String getId() {
        return "plugins.codegenerator";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "CodeGenerator";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (codeGeneratorConfig == null) {
            codeGeneratorConfig = new CodeGeneratorConfig(settings);
        }

        if (includeConfig == null) {
            includeConfig = new IncludeConfig(settings);
        }

        if (mainPaneConfig == null) {
            mainPaneConfig = new MainPaneConfig(codeGeneratorConfig, includeConfig);
        }

        return mainPaneConfig.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return isCodeGeneratorModified() || isIncludeModified();
    }

    private boolean isCodeGeneratorModified() {
        if (codeGeneratorConfig == null) {
            return false;
        }

        var templates = codeGeneratorConfig.getTabTemplates();
        if (settings.getCodeTemplates().size() != templates.size()) {
            return true;
        }

        for (var template : templates) {
            var codeTemplate = settings.getCodeTemplate(template.getId());
            if (codeTemplate.isEmpty() || !codeTemplate.get().equals(template)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncludeModified() {
        if (includeConfig == null) {
            return false;
        }

        var includes = includeConfig.getIncludes();
        if (settings.getIncludes().size() != includes.size()) {
            return true;
        }

        for (var include : includes) {
            var includesSetting = settings.getInclude(include.getId());
            if (includesSetting.isEmpty() || !includesSetting.get().equals(include)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        var templates = codeGeneratorConfig.getTabTemplates();
        for (var template : templates) {
            if (!template.isValid()) {
                throw new ConfigurationException(
                        "Not property can be empty and classNumber should be a number");
            }
        }

        settings.setCodeTemplates(templates);
        settings.setIncludes(includeConfig.getIncludes());

        codeGeneratorConfig.refresh(templates);
        includeConfig.refresh(includeConfig.getIncludes());
    }

    @Override
    public void reset() {

    }
}
