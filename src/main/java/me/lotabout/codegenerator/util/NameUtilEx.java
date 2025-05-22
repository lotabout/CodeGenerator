package me.lotabout.codegenerator.util;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.util.text.Matcher;

/**
 * Provides functions in {@link NameUtil}, with some additional functions.
 *
 * @author Haixing Hu
 */
public final class NameUtilEx {

  @NotNull
  public static List<String> nameToWordsLowerCase(@NotNull final String name) {
    return NameUtil.nameToWordsLowerCase(name);
  }

  @NotNull
  public static String buildRegexp(@NotNull final String pattern,
      final int exactPrefixLen, final boolean allowToUpper, final boolean allowToLower) {
    return NameUtil.buildRegexp(pattern, exactPrefixLen, allowToUpper, allowToLower);
  }

  @NotNull
  public static String buildRegexp(@NotNull final String pattern, final int exactPrefixLen,
      final boolean allowToUpper, final boolean allowToLower, final boolean lowerCaseWords,
      final boolean forCompletion) {
    return NameUtil.buildRegexp(pattern, exactPrefixLen, allowToUpper, allowToLower, lowerCaseWords, forCompletion);
  }

  @NotNull
  public static List<String> getSuggestionsByName(@NotNull final String name,
      @NotNull final String prefix, @NotNull final String suffix,
      final boolean upperCaseStyle, final boolean preferLongerNames, final boolean isArray) {
    return NameUtil.getSuggestionsByName(name, prefix, suffix, upperCaseStyle, preferLongerNames, isArray);
  }

  @NotNull
  public static String[] splitNameIntoWords(@NotNull final String name) {
    return NameUtil.splitNameIntoWords(name);
  }

  @NotNull
  public static String[] nameToWords(@NotNull final String name) {
    return NameUtil.nameToWords(name);
  }

  public static Matcher buildMatcher(@NotNull final String pattern,
      final int exactPrefixLen, final boolean allowToUpper, final boolean allowToLower) {
    return NameUtil.buildMatcher(pattern, exactPrefixLen, allowToUpper, allowToLower);
  }

  @NotNull
  public static String capitalizeAndUnderscore(@NotNull final String name) {
    return NameUtil.capitalizeAndUnderscore(name);
  }

  /**
   * Convert a name to lowercase and underscore style.
   *
   * @param name
   *     the name to convert.
   * @return
   *     the converted name.
   * @author Haixing Hu
   */
  @NotNull
  public static String lowercaseAndUnderscore(@NotNull final String name) {
    return NameUtil.splitWords(name, '_', Strings::toLowerCase);
  }

  /**
   * Get the getter name of a field.
   *
   * @param field
   *     the field.
   * @return
   *     the getter name of the field.
   * @author Haixing Hu
   */
  @NotNull
  public static String getGetterName(@NotNull final FieldEntry field) {
    final String name = StringUtil.capitalizeWithJavaBeanConvention(field.getName());
    if (field.isBoolean()) {
      return "is" + name;
    } else {
      return "get" + name;
    }
  }

  /**
   * Get the setter name of a field.
   *
   * @param field
   *     the field.
   * @return
   *     the setter name of the field.
   * @author Haixing Hu
   */
  @NotNull
  public static String getSetterName(@NotNull final FieldEntry field) {
    return "set" + StringUtil.capitalizeWithJavaBeanConvention(field.getName());
  }
}
