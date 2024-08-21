package me.lotabout.codegenerator.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
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
    private static final Logger logger = Logger.getInstance(GenerationUtil.class);

    /**
     * Combines the two lists into one list of members.
     *
     * @param filteredFields  fields to be included in the dialog
     * @param filteredMethods methods to be included in the dialog
     * @return the combined list
     */
    public static PsiElementClassMember[] combineToClassMemberList(final PsiField[] filteredFields, final PsiMethod[] filteredMethods) {
        final var members = new PsiElementClassMember[filteredFields.length + filteredMethods.length];

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
        final List fieldElements = EntryUtils.getOnlyAsFieldEntries(members, notNullMembers, false);
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
            final Map<String, Object> outputContext,
            String template,
            final List<Include> includes) throws GenerateCodeException {
        if (template == null) {
            return null;
        }

        final var sw = new StringWriter();
        try {
            final var vc = new VelocityContext();

            vc.put("settings", CodeStyle.getSettings(project));
            vc.put("project", project);
            vc.put("helper", GenerationHelper.class);
            vc.put("StringUtil", StringUtil.class);
            vc.put("NameUtil", NameUtil.class);
            vc.put("PsiShortNamesCache", PsiShortNamesCache.class);
            vc.put("JavaPsiFacade", JavaPsiFacade.class);
            vc.put("GlobalSearchScope", GlobalSearchScope.class);
            vc.put("EntryFactory", EntryFactory.class);

            for (final var paramName : contextMap.keySet()) {
                vc.put(paramName, contextMap.get(paramName));
            }

            template = updateTemplateWithIncludes(template, includes);
            if (logger.isDebugEnabled()) logger.debug("Velocity Template:\n" + template);

            // velocity
            final var velocity = VelocityFactory.getVelocityEngine();
            logger.debug("Executing velocity +++ START +++");
            velocity.evaluate(vc, sw, GenerationUtil.class.getName(), template);
            logger.debug("Executing velocity +++ END +++");

            if (outputContext != null) {
                for (final var key : vc.getKeys()) {
                    if (key instanceof String) {
                        outputContext.put((String) key, vc.get((String) key));
                    }
                }
            }
        } catch (final ProcessCanceledException e) {
            throw e;
        } catch (final Exception e) {
            throw new GenerateCodeException("Error in Velocity code generator", e);
        }

        return StringUtil.convertLineSeparators(sw.getBuffer().toString());
    }

    @NotNull
    private static String updateTemplateWithIncludes(final String template, final List<Include> includes) {
        final var includeLookups = getParsedIncludeLookupItems(includes);
        final var defaultImportParseExpression = includeLookups.stream()
                                                               .filter(IncludeLookupItem::isDefaultInclude)
                                                               .map(i -> String.format("#parse(%s)", i.getName()))
                                                               .collect(Collectors.joining(System.getProperty("line.separator")));
        final var templateWithDefaultImports = defaultImportParseExpression + System.getProperty("line.separator") + template;
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
                .collect(Collectors.joining(System.getProperty("line.separator")));
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
        private boolean defaultInclude;

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
