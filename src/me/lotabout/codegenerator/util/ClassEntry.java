package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.java.generate.element.ClassElement;

import java.util.Collections;
import java.util.List;

public class ClassEntry {
    private PsiClass raw;
    private ClassElement element;

    private String packageName;
    private List<String> importList;
    private List<FieldEntry> fields;
    private List<FieldEntry> allFields;
    private List<MethodEntry> methods;
    private List<MethodEntry> allMethods;
    private List<ClassEntry> innerClasses;
    private List<ClassEntry> allInnerClasses;
    private List<String> typeParamList = Collections.emptyList();

    public static ClassEntry of(PsiClass clazz, ClassElement element) {
        PsiFile psiFile = clazz.getContainingFile();
        ClassEntry entry = new ClassEntry();
        entry.setRaw(clazz);
        entry.setElement(element);
        entry.setPackageName(((PsiClassOwner)psiFile).getPackageName());
        entry.setImportList(GenerationUtil.getImportList((PsiJavaFile) psiFile));
        entry.setFields(GenerationUtil.getFields(clazz));
        entry.setAllFields(GenerationUtil.getAllFields(clazz));
        entry.setMethods(GenerationUtil.getMethods(clazz));
        entry.setAllMethods(GenerationUtil.getAllMethods(clazz));
        entry.setInnerClasses(GenerationUtil.getInnerClasses(clazz));
        entry.setAllInnerClasses(GenerationUtil.getAllInnerClasses(clazz));
        entry.setTypeParamList(GenerationUtil.getClassTypeParameters(clazz));
        return entry;
    }

    public PsiClass getRaw() {
        return raw;
    }

    public void setRaw(PsiClass raw) {
        this.raw = raw;
    }

    public ClassElement getElement() {
        return element;
    }

    public void setElement(ClassElement element) {
        this.element = element;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getImportList() {
        return importList;
    }

    public void setImportList(List<String> importList) {
        this.importList = importList;
    }

    public List<FieldEntry> getFields() {
        return fields;
    }

    public void setFields(List<FieldEntry> fields) {
        this.fields = fields;
    }

    public List<FieldEntry> getAllFields() {
        return allFields;
    }

    public void setAllFields(List<FieldEntry> allFields) {
        this.allFields = allFields;
    }

    public List<MethodEntry> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodEntry> methods) {
        this.methods = methods;
    }

    public List<MethodEntry> getAllMethods() {
        return allMethods;
    }

    public void setAllMethods(List<MethodEntry> allMethods) {
        this.allMethods = allMethods;
    }

    public List<ClassEntry> getInnerClasses() {
        return innerClasses;
    }

    public void setInnerClasses(List<ClassEntry> innerClasses) {
        this.innerClasses = innerClasses;
    }

    public List<ClassEntry> getAllInnerClasses() {
        return allInnerClasses;
    }

    public void setAllInnerClasses(List<ClassEntry> allInnerClasses) {
        this.allInnerClasses = allInnerClasses;
    }

    public List<String> getTypeParamList() {
        return typeParamList;
    }

    public void setTypeParamList(List<String> typeParamList) {
        this.typeParamList = typeParamList;
    }

    public boolean isImplements(String s) {
        return element.isImplements(s);
    }

    public boolean isExtends(String s) {
        return element.isExtends(s);
    }

    public boolean matchName(String s) throws IllegalArgumentException {
        return element.matchName(s);
    }

    public String[] getImplementNames() {
        return element.getImplementNames();
    }

    public void setImplementNames(String[] strings) {
        element.setImplementNames(strings);
    }

    public String getSuperQualifiedName() {
        return element.getSuperQualifiedName();
    }

    public void setSuperQualifiedName(String s) {
        element.setSuperQualifiedName(s);
    }

    public String getSuperName() {
        return element.getSuperName();
    }

    public void setSuperName(String s) {
        element.setSuperName(s);
    }

    public String getName() {
        return element.getName();
    }

    public void setName(String s) {
        element.setName(s);
    }

    public String getQualifiedName() {
        return element.getQualifiedName();
    }

    public void setQualifiedName(String s) {
        element.setQualifiedName(s);
    }

    public boolean isHasSuper() {
        return element.isHasSuper();
    }

    public boolean isDeprecated() {
        return element.isDeprecated();
    }

    public void setDeprecated(boolean b) {
        element.setDeprecated(b);
    }

    public boolean isEnum() {
        return element.isEnum();
    }

    public void setEnum(boolean b) {
        element.setEnum(b);
    }

    public boolean isException() {
        return element.isException();
    }

    public void setException(boolean b) {
        element.setException(b);
    }

    public boolean isAbstract() {
        return element.isAbstract();
    }

    public void setAbstract(boolean b) {
        element.setAbstract(b);
    }

    public void setTypeParams(int i) {
        element.setTypeParams(i);
    }

    public int getTypeParams() {
        return element.getTypeParams();
    }
}
