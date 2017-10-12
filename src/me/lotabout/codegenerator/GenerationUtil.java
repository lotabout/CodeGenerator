package me.lotabout.codegenerator;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.util.containers.ContainerUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.*;
import org.jetbrains.java.generate.exception.GenerateCodeException;
import org.jetbrains.java.generate.psi.PsiAdapter;
import org.jetbrains.java.generate.velocity.VelocityFactory;

import java.io.StringWriter;
import java.util.*;

public class GenerationUtil {
    private static final Logger logger = Logger.getInstance("#" + GenerationUtil.class.getName());

    /**
     * Combines the two lists into one list of members.
     *
     * @param filteredFields  fields to be included in the dialog
     * @param filteredMethods methods to be included in the dialog
     * @return the combined list
     */
    public static PsiElementClassMember[] combineToClassMemberList(PsiField[] filteredFields, PsiMethod[] filteredMethods) {
        PsiElementClassMember[] members = new PsiElementClassMember[filteredFields.length + filteredMethods.length];

        // first add fields
        for (int i = 0; i < filteredFields.length; i++) {
            members[i] = new PsiFieldMember(filteredFields[i]);
        }

        // then add methods
        for (int i = 0; i < filteredMethods.length; i++) {
            members[filteredFields.length + i] = new PsiMethodMember(filteredMethods[i]);
        }

        return members;
    }

    public static List<PsiMember> convertClassMembersToPsiMembers(@Nullable List<PsiElementClassMember> classMemberList) {
        if (classMemberList == null || classMemberList.isEmpty()) {
            return Collections.emptyList();
        }
        List<PsiMember> psiMemberList = new ArrayList<>();

        for (PsiElementClassMember classMember : classMemberList) {
            psiMemberList.add(classMember.getElement());
        }

        return psiMemberList;
    }

    /**
     * Generates the code using Velocity.
     * <p>
     * This is used to create the {@code toString} method body and it's javadoc.
     *
     * @param clazz
     * @param selectedMembers       the selected members as both {@link PsiField} and {@link PsiMethod}.
     * @param params                additional parameters stored with key/value in the map.
     * @param templateMacro         the velocity macro template
     * @param sortElements
     * @param useFullyQualifiedName @return code (usually javacode). Returns null if templateMacro is null.
     * @throws GenerateCodeException is thrown when there is an error generating the javacode.
     */
    public static String velocityGenerateCode(PsiClass clazz,
                                              Collection<? extends PsiMember> selectedMembers,
                                              Map<String, String> params,
                                              String templateMacro,
                                              int sortElements,
                                              boolean useFullyQualifiedName)
            throws GenerateCodeException {
        return velocityGenerateCode(clazz, selectedMembers, Collections.<PsiMember>emptyList(), params, Collections.<String, Object>emptyMap(), templateMacro, sortElements, useFullyQualifiedName, false);
    }

    /**
     * Generates the code using Velocity.
     * <p/>
     * This is used to create the {@code toString} method body and it's javadoc.
     *
     * @param selectedMembers the selected members as both {@link PsiField} and {@link PsiMethod}.
     * @param params          additional parameters stored with key/value in the map.
     * @param templateMacro   the velocity macro template
     * @param useAccessors    if true, accessor property for FieldElement bean would be assigned to field getter name append with ()
     * @return code (usually javacode). Returns null if templateMacro is null.
     * @throws GenerateCodeException is thrown when there is an error generating the javacode.
     */
    public static String velocityGenerateCode(@Nullable PsiClass clazz,
                                              Collection<? extends PsiMember> selectedMembers,
                                              Collection<? extends PsiMember> selectedNotNullMembers,
                                              Map<String, String> params,
                                              Map<String, Object> contextMap,
                                              String templateMacro,
                                              int sortElements,
                                              boolean useFullyQualifiedName,
                                              boolean useAccessors)
            throws GenerateCodeException {
        if (templateMacro == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        try {
            VelocityContext vc = new VelocityContext();

            // field information
            logger.debug("Velocity Context - adding fields");
            final List<FieldElement> fieldElements = ElementUtils.getOnlyAsFieldElements(selectedMembers, selectedNotNullMembers, useAccessors);
            vc.put("fields", fieldElements);
            if (fieldElements.size() == 1) {
                vc.put("field", fieldElements.get(0));
            }

            PsiMember member = clazz != null ? clazz : ContainerUtil.getFirstItem(selectedMembers);

            // method information
            logger.debug("Velocity Context - adding methods");
            vc.put("methods", ElementUtils.getOnlyAsMethodElements(selectedMembers));

            // element information (both fields and methods)
            logger.debug("Velocity Context - adding members (fields and methods)");
            List<Element> elements = ElementUtils.getOnlyAsFieldAndMethodElements(selectedMembers, selectedNotNullMembers, useAccessors);
            // sort elements if enabled and not using chooser dialog
            if (sortElements != 0) {
                Collections.sort(elements, new ElementComparator(sortElements));
            }
            vc.put("members", elements);

            // class information
            if (clazz != null) {
                ClassElement ce = ElementFactory.newClassElement(clazz);
                vc.put("class", ce);
                if (logger.isDebugEnabled()) logger.debug("Velocity Context - adding class: " + ce);

                // information to keep as it is to avoid breaking compatibility with prior releases
                vc.put("classname", useFullyQualifiedName ? ce.getQualifiedName() : ce.getName());
                vc.put("FQClassname", ce.getQualifiedName());
            }

            if (member != null) {
                vc.put("java_version", PsiAdapter.getJavaVersion(member));
                final Project project = member.getProject();
                vc.put("settings", CodeStyleSettingsManager.getSettings(project));
                vc.put("project", project);
            }

            vc.put("helper", GenerationHelper.class);
            vc.put("StringUtil", StringUtil.class);
            vc.put("NameUtil", NameUtil.class);

            for (String paramName : contextMap.keySet()) {
                vc.put(paramName, contextMap.get(paramName));
            }

            if (logger.isDebugEnabled()) logger.debug("Velocity Macro:\n" + templateMacro);

            // velocity
            VelocityEngine velocity = VelocityFactory.getVelocityEngine();
            logger.debug("Executing velocity +++ START +++");
            velocity.evaluate(vc, sw, CodeGeneratorWorker.class.getName(), templateMacro);
            logger.debug("Executing velocity +++ END +++");

            // any additional packages to import returned from velocity?
            if (vc.get("autoImportPackages") != null) {
                params.put("autoImportPackages", (String)vc.get("autoImportPackages"));
            }
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (Exception e) {
            throw new GenerateCodeException("Error in Velocity code generator", e);
        }

        return StringUtil.convertLineSeparators(sw.getBuffer().toString());
    }
}
