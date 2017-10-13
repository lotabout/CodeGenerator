package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.java.generate.element.ClassElement;
import org.jetbrains.java.generate.element.FieldElement;
import org.jetbrains.java.generate.element.MethodElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClassEntry {
    private String name;
    private PsiClass clazz;
    private String packageName;
    private List<String> importList;
    private ClassElement classElement;
    private List<FieldElement> fields;
    private List<FieldElement> allFields;
    private List<MethodElement> methods;
    private List<MethodElement> allMethods;
    private List<ClassEntry> innerClasses;
    private List<ClassEntry> allInnerClasses;
    private List<String> typeParams = Collections.emptyList();

    public static ClassEntry of(PsiClass clazz) {
        PsiFile psiFile = clazz.getContainingFile();
        ClassEntry entry = new ClassEntry();
        entry.setName(clazz.getName());
        entry.setClazz(clazz);
        entry.setPackageName(((PsiClassOwner)psiFile).getPackageName());
        entry.setImportList(GenerationUtil.getImportList((PsiJavaFile) psiFile));
        entry.setFields(GenerationUtil.getFields(clazz));
        entry.setAllFields(GenerationUtil.getAllFields(clazz));
        entry.setMethods(GenerationUtil.getMethods(clazz));
        entry.setAllMethods(GenerationUtil.getAllMethods(clazz));
        entry.setInnerClasses(Arrays.stream(clazz.getInnerClasses())
                .map(ClassEntry::of)
                .collect(Collectors.toList()));
        entry.setAllInnerClasses(Arrays.stream(clazz.getAllInnerClasses())
                .map(ClassEntry::of)
                .collect(Collectors.toList()));
        return entry;
    }

    public PsiClass getClazz() {
        return clazz;
    }

    public void setClazz(PsiClass clazz) {
        this.clazz = clazz;
    }

    public List<ClassEntry> getAllInnerClasses() {
        return allInnerClasses;
    }

    private void setAllInnerClasses(List<ClassEntry> allInnerClasses) {
        this.allInnerClasses = allInnerClasses;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    private void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getImportList() {
        return importList;
    }

    private void setImportList(List<String> importList) {
        this.importList = importList;
    }

    public ClassElement getClassElement() {
        return classElement;
    }

    public void setClassElement(ClassElement classElement) {
        this.classElement = classElement;
    }

    public List<FieldElement> getFields() {
        return fields;
    }

    private void setFields(List<FieldElement> fields) {
        this.fields = fields;
    }

    public List<FieldElement> getAllFields() {
        return allFields;
    }

    private void setAllFields(List<FieldElement> allFields) {
        this.allFields = allFields;
    }

    public List<MethodElement> getMethods() {
        return methods;
    }

    private void setMethods(List<MethodElement> methods) {
        this.methods = methods;
    }

    public List<MethodElement> getAllMethods() {
        return allMethods;
    }

    private void setAllMethods(List<MethodElement> allMethods) {
        this.allMethods = allMethods;
    }

    public List<ClassEntry> getInnerClasses() {
        return innerClasses;
    }

    private void setInnerClasses(List<ClassEntry> innerClasses) {
        this.innerClasses = innerClasses;
    }

    public List<String> getTypeParams() {
        return typeParams;
    }

    private void setTypeParams(List<String> typeParams) {
        this.typeParams = typeParams;
    }
}
