package me.lotabout.codegenerator;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ApplicationComponent;

public class CodeGenerator implements ApplicationComponent {
    public CodeGenerator() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return CodeGenerator.class.getCanonicalName();
    }
}
