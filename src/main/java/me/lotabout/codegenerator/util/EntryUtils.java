package me.lotabout.codegenerator.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;

public class EntryUtils {

    public static List<MemberEntry<?>> getOnlyAsFieldAndMethodElements(
            final Collection<? extends PsiMember> members,
            final boolean useAccessors) {
        final List<MemberEntry<?>> entryList = new ArrayList<>();
        for (final PsiMember member : members) {
            MemberEntry<?> entry = null;
            if (member instanceof PsiField) {
                entry = EntryFactory.of((PsiField) member, useAccessors);
            } else if (member instanceof PsiMethod) {
                entry = EntryFactory.of((PsiMethod) member);
            }

            if (entry != null) {
                entryList.add(entry);
            }
        }
        return entryList;
    }

    public static List<FieldEntry> getOnlyAsFieldEntries(
            final Collection<? extends PsiMember> members,
            final boolean useAccessors) {
        final List<FieldEntry> fieldEntryList = new ArrayList<>();

        for (final PsiMember member : members) {
            if (member instanceof final PsiField field) {
                final FieldEntry fe = EntryFactory.of(field, useAccessors);
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
