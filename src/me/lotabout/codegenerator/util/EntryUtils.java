package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import org.jetbrains.java.generate.element.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntryUtils {
    public static List<MemberEntry> getOnlyAsFieldAndMethodElements(Collection<? extends PsiMember> members,
                                                                Collection<? extends PsiMember> selectedNotNullMembers,
                                                                boolean useAccessors) {
        List<MemberEntry>
                entryList = new ArrayList<>();

        for (PsiMember member : members) {
            MemberEntry entry = null;
            if (member instanceof PsiField) {
                FieldEntry fieldEntry= EntryFactory.newFieldEntry((PsiField) member, useAccessors);
                if (selectedNotNullMembers.contains(member)) {
                    fieldEntry.setNotNull(true);
                }
                entry = fieldEntry;
            } else if (member instanceof PsiMethod) {
                MethodEntry methodEntry = EntryFactory.newMethodEntry((PsiMethod) member);
                if (selectedNotNullMembers.contains(member)) {
                    methodEntry.setNotNull(true);
                }
                entry = methodEntry;
            }

            if (entry != null) {
                entryList.add(entry);
            }
        }
        return entryList;
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
