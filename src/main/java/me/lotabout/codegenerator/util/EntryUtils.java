package me.lotabout.codegenerator.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;

public class EntryUtils {

    public static List<MemberEntry> getOnlyAsFieldAndMethodElements(
            final Collection<? extends PsiMember> members,
            final Collection<? extends PsiMember> selectedNotNullMembers,
            final boolean useAccessors) {
        final List<MemberEntry> entryList = new ArrayList<>();
        for (final PsiMember member : members) {
            MemberEntry entry = null;
            if (member instanceof PsiField) {
                final FieldEntry fieldEntry= EntryFactory.of((PsiField) member, useAccessors);
                if (selectedNotNullMembers.contains(member)) {
                    fieldEntry.setNotNull(true);
                }
                entry = fieldEntry;
            } else if (member instanceof PsiMethod) {
                final MethodEntry methodEntry = EntryFactory.of((PsiMethod) member);
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

    public static List<FieldEntry> getOnlyAsFieldEntries(
            final Collection<? extends PsiMember> members,
            final Collection<? extends PsiMember> selectedNotNullMembers,
            final boolean useAccessors) {
        final List<FieldEntry> fieldEntryList = new ArrayList<>();

        for (final PsiMember member : members) {
            if (member instanceof final PsiField field) {
              final FieldEntry fe = EntryFactory.of(field, useAccessors);
                if (selectedNotNullMembers.contains(member)) {
                    fe.setNotNull(true);
                }
                fieldEntryList.add(fe);
            }
        }

        return fieldEntryList;
    }

    public static List<MethodEntry> getOnlyAsMethodEntries(final Collection<? extends PsiMember> members) {
        final List<MethodEntry> methodEntryList = new ArrayList<>();

        for (final PsiMember member : members) {
            if (member instanceof final PsiMethod method) {
                final MethodEntry me = EntryFactory.of(method);
                methodEntryList.add(me);
            }
        }

        return methodEntryList;
    }
}
