package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import org.jetbrains.java.generate.element.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntryUtils {
    public static List<Element> getOnlyAsFieldAndMethodElements(Collection<? extends PsiMember> members,
                                                                Collection<? extends PsiMember> selectedNotNullMembers,
                                                                boolean useAccessors) {
        List<Element> elementList = new ArrayList<>();

        for (PsiMember member : members) {
            Element element = null;
            if (member instanceof PsiField) {
                FieldEntry entry = EntryFactory.newFieldEntry((PsiField) member, useAccessors);
                if (selectedNotNullMembers.contains(member)) {
                    entry.setNotNull(true);
                }
                element = entry;
            } else if (member instanceof PsiMethod) {
                MethodEntry entry = EntryFactory.newMethodEntry((PsiMethod) member);
                if (selectedNotNullMembers.contains(member)) {
                    entry.setNotNull(true);
                }
                element = entry;
            }

            if (element != null) {
                elementList.add(element);
            }
        }
        return elementList;
    }

    public static List<FieldEntry> getOnlyAsFieldEntrys(Collection<? extends PsiMember> members,
                                                            Collection<? extends PsiMember> selectedNotNullMembers,
                                                            boolean useAccessors) {
        List<FieldEntry> fieldEntryList = new ArrayList<>();

        for (PsiMember member : members) {
            if (member instanceof PsiField) {
                PsiField field = (PsiField) member;
                FieldEntry fe = EntryFactory.newFieldEntry(field, useAccessors);
                if (selectedNotNullMembers.contains(member)) {
                    fe.setNotNull(true);
                }
                fieldEntryList.add(fe);
            }
        }

        return fieldEntryList;
    }

    public static List<MethodEntry> getOnlyAsMethodEntrys(Collection<? extends PsiMember> members) {
        List<MethodEntry> methodEntryList = new ArrayList<>();

        for (PsiMember member : members) {
            if (member instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) member;
                MethodEntry me = EntryFactory.newMethodEntry(method);
                methodEntryList.add(me);
            }
        }

        return methodEntryList;
    }
}
