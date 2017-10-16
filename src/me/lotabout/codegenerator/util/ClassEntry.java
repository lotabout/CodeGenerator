package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.java.generate.element.ClassElement;

import java.util.ArrayList;
import java.util.List;

public class ClassEntry {
    private PsiClass raw;
    private ClassElement element;

    private String packageName;
    private List<String> importList;
    private List<FieldEntry> fields = new ArrayList<>();
    private List<FieldEntry> allFields = new ArrayList<>();
    private List<MethodEntry> methods = new ArrayList<>();
    private List<MethodEntry> allMethods = new ArrayList<>();
    private List<ClassEntry> innerClasses = new ArrayList<>();
    private List<ClassEntry> allInnerClasses = new ArrayList<>();
    private List<MemberEntry> members = new ArrayList<>();
    private List<MemberEntry> allMembers = new ArrayList<>();
    private List<String> typeParamList;

    public static ClassEntry of(PsiClass clazz, ClassElement element) {
        PsiFile psiFile = clazz.getContainingFile();
        ClassEntry entry = new ClassEntry();
        entry.setRaw(clazz);
        entry.setElement(element);
        entry.setPackageName(((PsiClassOwner)psiFile).getPackageName());
        entry.setImportList(GenerationUtil.getImportList((PsiJavaFile) psiFile));
        entry.addFields(GenerationUtil.getFields(clazz));
        entry.addAllFields(GenerationUtil.getAllFields(clazz));
        entry.addMethod(GenerationUtil.getMethods(clazz));
        entry.addAllMethods(GenerationUtil.getAllMethods(clazz));
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

    public void addFields(List<FieldEntry> fields) {
        this.fields.addAll(fields);
        this.members.addAll(fields);
    }

    public List<FieldEntry> getAllFields() {
        return allFields;
    }

    public void addAllFields(List<FieldEntry> allFields) {
        this.allFields = allFields;
        this.allMembers.addAll(fields);
    }

    public List<MethodEntry> getMethods() {
        return methods;
    }

    public void addMethod(List<MethodEntry> methods) {
        this.methods.addAll(methods);
        this.members.addAll(methods);
    }

    public List<MethodEntry> getAllMethods() {
        return allMethods;
    }

    public void addAllMethods(List<MethodEntry> allMethods) {
        this.allMethods.addAll(allMethods);
        this.allMembers.addAll(allMethods);
    }

    public List<ClassEntry> getInnerClasses() {
        return innerClasses;
    }

    public List<MemberEntry> getMembers() {
        return members;
    }

    public List<MemberEntry> getAllMembers() {
        return allMembers;
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

    @Override
    public String toString() {
        return "ClassEntry{" +
                "raw=" + raw +
                ", element=" + element +
                ", packageName='" + packageName + '\'' +
                ", importList=" + importList +
                ", fields=" + fields +
                ", allFields=" + allFields +
                ", methods=" + methods +
                ", allMethods=" + allMethods +
                ", innerClasses=" + innerClasses +
                ", allInnerClasses=" + allInnerClasses +
                ", members=" + members +
                ", allMembers=" + allMembers +
                ", typeParamList=" + typeParamList +
                '}';
    }
}
