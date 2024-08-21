package me.lotabout.codegenerator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.java.generate.element.ClassElement;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

public class ClassEntry {
    private PsiClass raw;
    private ClassElement element;

    private String packageName;
    private List<String> importList;
    private List<FieldEntry> fields = new ArrayList<>();
    private List<FieldEntry> allFields = new ArrayList<>();
    private List<MethodEntry> methods = new ArrayList<>();
    private List<MethodEntry> allMethods = new ArrayList<>();
    private List<MemberEntry> members = new ArrayList<>();
    private List<MemberEntry> allMembers = new ArrayList<>();
    private List<String> typeParamList;

    public static ClassEntry of(final PsiClass clazz, final ClassElement element) {
        final PsiFile psiFile = clazz.getContainingFile();
        final ClassEntry entry = new ClassEntry();
        entry.setRaw(clazz);
        entry.setElement(element);
        entry.setPackageName(((PsiClassOwner)psiFile).getPackageName());
        entry.setImportList(GenerationUtil.getImportList((PsiJavaFile) psiFile));
        entry.addFields(GenerationUtil.getFields(clazz));
        entry.addAllFields(GenerationUtil.getAllFields(clazz));
        entry.addMethod(GenerationUtil.getMethods(clazz));
        entry.addAllMethods(GenerationUtil.getAllMethods(clazz));
        entry.setTypeParamList(GenerationUtil.getClassTypeParameters(clazz));
        return entry;
    }

    public List<FieldEntry> getFields() {
        return fields;
    }

    public void setFields(final List<FieldEntry> fields) {
        this.fields = fields;
    }

    public void setAllFields(final List<FieldEntry> allFields) {
        this.allFields = allFields;
    }

    public void setMethods(final List<MethodEntry> methods) {
        this.methods = methods;
    }

    public void setAllMethods(final List<MethodEntry> allMethods) {
        this.allMethods = allMethods;
    }

    public void setMembers(final List<MemberEntry> members) {
        this.members = members;
    }

    public void setAllMembers(final List<MemberEntry> allMembers) {
        this.allMembers = allMembers;
    }

    public PsiClass getRaw() {
        return raw;
    }

    public void setRaw(final PsiClass raw) {
        this.raw = raw;
    }

    public ClassElement getElement() {
        return element;
    }

    public void setElement(final ClassElement element) {
        this.element = element;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public List<String> getImportList() {
        return importList;
    }

    public void setImportList(final List<String> importList) {
        this.importList = importList;
    }

    public void addFields(final List<FieldEntry> fields) {
        this.fields.addAll(fields);
        this.members.addAll(fields);
    }

    public List<FieldEntry> getAllFields() {
        return allFields;
    }

    public void addAllFields(final List<FieldEntry> allFields) {
        this.allFields = allFields;
        this.allMembers.addAll(fields);
    }

    public List<MethodEntry> getMethods() {
        return methods;
    }

    public void addMethod(final List<MethodEntry> methods) {
        this.methods.addAll(methods);
        this.members.addAll(methods);
    }

    public List<MethodEntry> getAllMethods() {
        return allMethods;
    }

    public void addAllMethods(final List<MethodEntry> allMethods) {
        this.allMethods.addAll(allMethods);
        this.allMembers.addAll(allMethods);
    }

    public List<ClassEntry> getInnerClasses() {
        return Arrays.stream(raw.getInnerClasses())
                .map(EntryFactory::of)
                .collect(Collectors.toList());
    }

    public List<MemberEntry> getMembers() {
        return members;
    }

    public List<MemberEntry> getAllMembers() {
        return allMembers;
    }

    public List<ClassEntry> getAllInnerClasses() {
        // lazily turn all inner classes into class entry
        return Arrays.stream(raw.getAllInnerClasses())
                .map(EntryFactory::of)
                .collect(Collectors.toList());
    }

    public List<String> getTypeParamList() {
        return typeParamList;
    }

    public void setTypeParamList(final List<String> typeParamList) {
        this.typeParamList = typeParamList;
    }

    public boolean isImplements(final String s) {
        return element.isImplements(s);
    }

    public boolean isExtends(final String s) {
        return element.isExtends(s);
    }

    public boolean matchName(final String s) throws IllegalArgumentException {
        return element.matchName(s);
    }

    public String[] getImplementNames() {
        return element.getImplementNames();
    }

    public void setImplementNames(final String[] strings) {
        element.setImplementNames(strings);
    }

    public String getSuperQualifiedName() {
        return element.getSuperQualifiedName();
    }

    public void setSuperQualifiedName(final String s) {
        element.setSuperQualifiedName(s);
    }

    public String getSuperName() {
        return element.getSuperName();
    }

    public void setSuperName(final String s) {
        element.setSuperName(s);
    }

    public String getName() {
        return element.getName();
    }

    public void setName(final String s) {
        element.setName(s);
    }

    public String getQualifiedName() {
        return element.getQualifiedName();
    }

    public void setQualifiedName(final String s) {
        element.setQualifiedName(s);
    }

    public boolean isHasSuper() {
        return element.isHasSuper();
    }

    public boolean isDeprecated() {
        return element.isDeprecated();
    }

    public void setDeprecated(final boolean b) {
        element.setDeprecated(b);
    }

    public boolean isEnum() {
        return element.isEnum();
    }

    public void setEnum(final boolean b) {
        element.setEnum(b);
    }

    public boolean isException() {
        return element.isException();
    }

    public void setException(final boolean b) {
        element.setException(b);
    }

    public boolean isAbstract() {
        return element.isAbstract();
    }

    public void setAbstract(final boolean b) {
        element.setAbstract(b);
    }

    public void setTypeParams(final int i) {
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
                ", members=" + members +
                ", allMembers=" + allMembers +
                ", typeParamList=" + typeParamList +
                '}';
    }


}
