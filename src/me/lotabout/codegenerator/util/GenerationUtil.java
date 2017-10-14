package me.lotabout.codegenerator.util;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.containers.ContainerUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.*;
import org.jetbrains.java.generate.exception.GenerateCodeException;
import org.jetbrains.java.generate.exception.PluginException;
import org.jetbrains.java.generate.psi.PsiAdapter;
import org.jetbrains.java.generate.velocity.VelocityFactory;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

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

    public static String velocityEvaluate(
            @Nullable PsiClass clazz,
            Map<String, Object> contextMap,
            Map<String, Object> outputContext,
            String templateMacro) throws GenerateCodeException {
        if (templateMacro == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        try {
            VelocityContext vc = new VelocityContext();

            final Project project = clazz.getProject();
            vc.put("settings", CodeStyleSettingsManager.getSettings(project));
            vc.put("project", project);
            vc.put("java_version", PsiAdapter.getJavaVersion(clazz));
            vc.put("helper", GenerationHelper.class);
            vc.put("StringUtil", StringUtil.class);
            vc.put("NameUtil", NameUtil.class);
            vc.put("PsiShortNamesCache", PsiShortNamesCache.class);
            vc.put("JavaPsiFacade", JavaPsiFacade.class);
            vc.put("GlobalSearchScope", GlobalSearchScope.class);
            vc.put("ElementFactory", ElementFactory.class);

            for (String paramName : contextMap.keySet()) {
                vc.put(paramName, contextMap.get(paramName));
            }

            if (logger.isDebugEnabled()) logger.debug("Velocity Macro:\n" + templateMacro);

            // velocity
            VelocityEngine velocity = VelocityFactory.getVelocityEngine();
            logger.debug("Executing velocity +++ START +++");
            velocity.evaluate(vc, sw, clazz.getName(), templateMacro);
            logger.debug("Executing velocity +++ END +++");

            if (outputContext != null) {
                for (Object key : vc.getKeys()) {
                    if (key instanceof String) {
                    outputContext.put((String) key, vc.get((String) key));
                    }
                }
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


    /**
     * Handles any exception during the executing on this plugin.
     *
     * @param project PSI project
     * @param e       the caused exception.
     * @throws RuntimeException is thrown for severe exceptions
     */
    public static void handleException(Project project, Exception e) throws RuntimeException {
        logger.info(e);

        if (e instanceof GenerateCodeException) {
            // code generation error - display velocity error in error dialog so user can identify problem quicker
            Messages.showMessageDialog(project,
                    "Velocity error generating code - see IDEA log for more details (stacktrace should be in idea.log):\n" +
                            e.getMessage(), "Warning", Messages.getWarningIcon());
        } else if (e instanceof PluginException) {
            // plugin related error - could be recoverable.
            Messages.showMessageDialog(project, "A PluginException was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n" + e.getMessage(), "Warning", Messages.getWarningIcon());
        } else if (e instanceof RuntimeException) {
            // unknown error (such as NPE) - not recoverable
            Messages.showMessageDialog(project, "An unrecoverable exception was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n" + e.getMessage(), "Error", Messages.getErrorIcon());
            throw (RuntimeException) e; // throw to make IDEA alert user
        } else {
            // unknown error (such as NPE) - not recoverable
            Messages.showMessageDialog(project, "An unrecoverable exception was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n" + e.getMessage(), "Error", Messages.getErrorIcon());
            throw new RuntimeException(e); // rethrow as runtime to make IDEA alert user
        }
    }

    static List<FieldEntry> getFields(PsiClass clazz) {
        return Arrays.stream(clazz.getFields())
                    .map(f -> EntryFactory.newFieldEntry(f, false))
                    .collect(Collectors.toList());
    }
    static List<FieldEntry> getAllFields(PsiClass clazz) {
        return Arrays.stream(clazz.getAllFields())
                .map(f -> EntryFactory.newFieldEntry(f, false))
                .collect(Collectors.toList());
    }

    static List<MethodEntry> getMethods(PsiClass clazz) {
        return Arrays.stream(clazz.getMethods())
                .map(EntryFactory::newMethodEntry)
                .collect(Collectors.toList());
    }
    static List<MethodEntry> getAllMethods(PsiClass clazz) {
        return Arrays.stream(clazz.getAllMethods())
                .map(EntryFactory::newMethodEntry)
                .collect(Collectors.toList());
    }

    static List<ClassEntry> getInnerClasses(PsiClass clazz) {
        return Arrays.stream(clazz.getInnerClasses())
                .map(EntryFactory::newClassEntry)
                .collect(Collectors.toList());
    }
    static List<ClassEntry> getAllInnerClasses(PsiClass clazz) {
        return Arrays.stream(clazz.getAllInnerClasses())
                .map(EntryFactory::newClassEntry)
                .collect(Collectors.toList());
    }

    static List<String> getImportList(PsiJavaFile javaFile) {
        PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(importList.getImportStatements())
                .map(PsiImportStatement::getQualifiedName)
                .collect(Collectors.toList());
    }

    static List<String> getClassTypeParameters(PsiClass psiClass) {
        return Arrays.stream(psiClass.getTypeParameters()).map(PsiNamedElement::getName).collect(Collectors.toList());
    }

}
