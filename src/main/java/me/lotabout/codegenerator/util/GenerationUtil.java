package me.lotabout.codegenerator.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.ElementComparator;
import org.jetbrains.java.generate.element.GenerationHelper;
import org.jetbrains.java.generate.exception.GenerateCodeException;
import org.jetbrains.java.generate.exception.PluginException;
import org.jetbrains.java.generate.velocity.VelocityFactory;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

import me.lotabout.codegenerator.config.include.Include;

public class GenerationUtil {

    public static final String VELOCITY_TOOLS_CONFIG = "/velocity-tools.xml";

    private static final Logger logger = Logger.getInstance(GenerationUtil.class);

    /**
     * Combines the two lists into one list of members.
     *
     * @param filteredFields  fields to be included in the dialog
     * @param filteredMethods methods to be included in the dialog
     * @return the combined list
     */
    public static PsiElementClassMember<?>[] combineToClassMemberList(final PsiField[] filteredFields,
        final PsiMethod[] filteredMethods) {
        final PsiElementClassMember<?>[] members =
            new PsiElementClassMember[filteredFields.length + filteredMethods.length];
        // first add fields
        for (var i = 0; i < filteredFields.length; i++) {
            members[i] = new PsiFieldMember(filteredFields[i]);
        }
        // then add methods
        for (var i = 0; i < filteredMethods.length; i++) {
            members[filteredFields.length + i] = new PsiMethodMember(filteredMethods[i]);
        }
        return members;
    }

    public static List<PsiMember> convertClassMembersToPsiMembers(@Nullable final List<PsiElementClassMember<?>> classMemberList) {
        if (classMemberList == null || classMemberList.isEmpty()) {
            return Collections.emptyList();
        }
        final List<PsiMember> psiMemberList = new ArrayList<>();
        for (final var classMember : classMemberList) {
            psiMemberList.add(classMember.getElement());
        }
        return psiMemberList;
    }

    public static void insertMembersToContext(final List<PsiMember> members,
            final List<PsiMember> notNullMembers, final Map<String, Object> context,
            final String postfix, final int sortElements) {
        logger.debug("insertMembersToContext - adding fields");
        // field information
        final List<FieldEntry> fieldElements = EntryUtils.getOnlyAsFieldEntries(members, notNullMembers, false);
        context.put("fields" + postfix, fieldElements);
        context.put("fields", fieldElements);
        if (fieldElements.size() == 1) {
            context.put("field" + postfix, fieldElements.get(0));
            context.put("field", fieldElements.get(0));
        }

        // method information
        logger.debug("insertMembersToContext - adding members");
        context.put("methods" + postfix, EntryUtils.getOnlyAsMethodEntries(members));
        context.put("methods", EntryUtils.getOnlyAsMethodEntries(members));

        // element information (both fields and methods)
        logger.debug("Velocity Context - adding members (fields and methods)");
        final var elements = EntryUtils.getOnlyAsFieldAndMethodElements(members, notNullMembers, false);
        // sort elements if enabled and not using chooser dialog
        if (sortElements != 0) {
            elements.sort(new ElementComparator(sortElements));
        }
        context.put("members" + postfix, elements);
        context.put("members", elements);
    }

    public static String velocityEvaluate(
            @NotNull final Project project,
            @NotNull final Map<String, Object> contextMap,
            @Nullable final Map<String, Object> outputContext,
            @Nullable final String template,
            @NotNull final List<Include> includes) throws GenerateCodeException {
        contextMap.put("settings", CodeStyle.getSettings(project));
        contextMap.put("project", project);
        contextMap.put("helper", GenerationHelper.class);
        contextMap.put("StringUtil", StringUtil.class);
        contextMap.put("StringUtilEx", StringUtilEx.class);
        contextMap.put("NameUtil", NameUtil.class);
        contextMap.put("NameUtilEx", NameUtilEx.class);
        contextMap.put("PsiShortNamesCache", PsiShortNamesCache.class);
        contextMap.put("JavaPsiFacade", JavaPsiFacade.class);
        contextMap.put("GlobalSearchScope", GlobalSearchScope.class);
        contextMap.put("EntryFactory", EntryFactory.class);
        return velocityEvaluate(contextMap, outputContext, template, includes);
    }

    // split this method to make unit testing easier
    public static String velocityEvaluate(
        @NotNull final Map<String, Object> contextMap,
        @Nullable final Map<String, Object> outputContext,
        @Nullable String template,
        @NotNull final List<Include> includes) throws GenerateCodeException {
        if (template == null) {
            return null;
        }
        final StringWriter sw = new StringWriter();
        try {
            final ToolManager toolManager = new ToolManager(false, true);
            toolManager.configure(VELOCITY_TOOLS_CONFIG);
            final Context vc = toolManager.createContext();
            for (final var paramName : contextMap.keySet()) {
                vc.put(paramName, contextMap.get(paramName));
            }
            template = updateTemplateWithIncludes(template, includes);
            logger.debug("Velocity Template:\n", template);
            // velocity
            final VelocityEngine velocity = VelocityFactory.getVelocityEngine();
            logger.debug("Executing velocity +++ START +++");
            velocity.evaluate(vc, sw, GenerationUtil.class.getName(), template);
            logger.debug("Executing velocity +++ END +++");
            if (outputContext != null) {
                for (final String key : vc.getKeys()) {
                    if (key != null) {
                        outputContext.put(key, vc.get(key));
                    }
                }
            }
        } catch (final ProcessCanceledException e) {
            logger.error("Error in Velocity code generator: " + e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            logger.error("Error in Velocity code generator: " + e.getMessage(), e);
            throw new GenerateCodeException("Error in Velocity code generator", e);
        }
        final String result = StringUtil.convertLineSeparators(sw.toString());
        logger.debug("Velocity Result:\n", result);
        return result;
    }

    @NotNull
    private static String updateTemplateWithIncludes(final String template, final List<Include> includes) {
        final var includeLookups = getParsedIncludeLookupItems(includes);
        final var defaultImportParseExpression = includeLookups
            .stream()
            .filter(IncludeLookupItem::isDefaultInclude)
            .map(i -> String.format("#parse(%s)", i.getName()))
            .collect(Collectors.joining(System.lineSeparator()));
        final var templateWithDefaultImports = defaultImportParseExpression + System.lineSeparator() + template;
        return replaceParseExpressions(templateWithDefaultImports, includeLookups);
    }

    @NotNull
    private static List<IncludeLookupItem> getParsedIncludeLookupItems(final List<Include> includes) {
        final var includeLookups = includes.stream()
                .map(include -> new IncludeLookupItem(include.getName(), include.getContent(), include.isDefaultInclude()))
                .collect(Collectors.toList());

        return includeLookups.stream()
                .map(i -> new IncludeLookupItem(i.getName(), replaceParseExpressions(i.getContent(), includeLookups), i.isDefaultInclude()))
                .collect(Collectors.toList());
    }

    @NotNull
    private static String replaceParseExpressions(@NotNull String template, @NotNull final List<IncludeLookupItem> includeLookupItems) {
        template = template.lines()//
                .map(line -> replaceParseExpression(line, includeLookupItems))//
                .collect(Collectors.joining(System.lineSeparator()));
        return template;
    }

    private static String replaceParseExpression(final String line, final List<IncludeLookupItem> includeLookupItems) {
        if (line.trim().startsWith("#parse")) {
            final var includeName = line.trim().replace("#parse(", "")
                                        .replace(")", "")
                                        .replaceAll("\"", "");
            final var includeContent = includeLookupItems.stream()
                                                         .filter(m -> m.getName().equals(includeName))
                                                         .map(IncludeLookupItem::getContent)
                                                         .findFirst();
            if (includeContent.isPresent()) {
                return includeContent.get();
            }
        }
        return line;
    }


    /**
     * Handles any exception during the executing on this plugin.
     *
     * @param project PSI project
     * @param e       the caused exception.
     * @throws RuntimeException is thrown for severe exceptions
     */
    public static void handleException(final Project project, final Exception e) throws RuntimeException {
        logger.info(e);

        if (e instanceof GenerateCodeException) {
            // code generation error - display velocity error in error dialog so user can identify problem quicker
            Messages.showMessageDialog(project,
                "Velocity error generating code - see IDEA log for more details (stacktrace should be in idea.log):\n" +
                e.getMessage(), "Warning", Messages.getWarningIcon());
        } else if (e instanceof PluginException) {
            // plugin related error - could be recoverable.
            Messages.showMessageDialog(project,
                "A PluginException was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n"
                    + e.getMessage(), "Warning", Messages.getWarningIcon());
        } else if (e instanceof RuntimeException) {
            // unknown error (such as NPE) - not recoverable
            Messages.showMessageDialog(project,
                "An unrecoverable exception was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n"
                    + e.getMessage(), "Error", Messages.getErrorIcon());
            throw (RuntimeException) e; // throw to make IDEA alert user
        } else {
            // unknown error (such as NPE) - not recoverable
            Messages.showMessageDialog(project,
                "An unrecoverable exception was thrown while performing the action - see IDEA log for details (stacktrace should be in idea.log):\n"
                    + e.getMessage(), "Error", Messages.getErrorIcon());
            throw new RuntimeException(e); // rethrow as runtime to make IDEA alert user
        }
    }

    static List<FieldEntry> getFields(final PsiClass clazz) {
        return Arrays.stream(clazz.getFields())
                .map(f -> EntryFactory.of(f, false))
                .collect(Collectors.toList());
    }

    static List<FieldEntry> getAllFields(final PsiClass clazz) {
        return Arrays.stream(clazz.getAllFields())
                .map(f -> EntryFactory.of(f, false))
                .collect(Collectors.toList());
    }

    static List<MethodEntry> getMethods(final PsiClass clazz) {
        return Arrays.stream(clazz.getMethods())
                .map(EntryFactory::of)
                .collect(Collectors.toList());
    }

    static List<MethodEntry> getAllMethods(final PsiClass clazz) {
        return Arrays.stream(clazz.getAllMethods())
                .map(EntryFactory::of)
                .collect(Collectors.toList());
    }

    static List<String> getImportList(final PsiJavaFile javaFile) {
        final var importList = javaFile.getImportList();
        if (importList == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(importList.getImportStatements())
                .map(PsiImportStatement::getQualifiedName)
                .collect(Collectors.toList());
    }

    static List<String> getClassTypeParameters(final PsiClass psiClass) {
        return Arrays.stream(psiClass.getTypeParameters()).map(PsiNamedElement::getName).collect(Collectors.toList());
    }

    static final class IncludeLookupItem {
        @NotNull
        private final String name;
        @NotNull
        private final String content;
        private final boolean defaultInclude;

        IncludeLookupItem(@NotNull final String name, @NotNull final String content, final boolean defaultInclude) {
            this.name = name;
            this.content = content;
            this.defaultInclude = defaultInclude;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getContent() {
            return content;
        }

        public boolean isDefaultInclude() {
            return defaultInclude;
        }
    }

}
