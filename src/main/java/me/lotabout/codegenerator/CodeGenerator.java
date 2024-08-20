package me.lotabout.codegenerator;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class CodeGenerator implements ApplicationComponent {
    public CodeGenerator() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "me.lotabout.codegenerator.CodeGenerator";
    }
}
