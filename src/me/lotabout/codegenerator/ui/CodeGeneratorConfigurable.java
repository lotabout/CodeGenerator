package me.lotabout.codegenerator.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CodeGeneratorConfigurable implements SearchableConfigurable {
    @NotNull @Override public String getId() {
        return null;
    }

    @Nls @Override public String getDisplayName() {
        return null;
    }

    @Nullable @Override public JComponent createComponent() {
        return null;
    }

    @Override public boolean isModified() {
        return false;
    }

    @Override public void apply() throws ConfigurationException {

    }
}
