package me.lotabout.codegenerator.util;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;

/**
 * Provides functions in {@link StringUtil}, with some additional functions.
 *
 * @author Haixing Hu
 */
public class StringUtilEx {

  private static final Logger logger = Logger.getInstance(StringUtilEx.class);

  @NotNull
  public static List<String> getWordsInStringLongestFirst(@NotNull final String find) {
    return StringUtil.getWordsInStringLongestFirst(find);
  }

  @NotNull
  public static String escapePattern(@NotNull final String text) {
    return StringUtil.escapePattern(text);
  }

  @NotNull
  public static String replace(@NotNull final String text, @NotNull final String oldS, @NotNull final String newS) {
    return StringUtil.replace(text, oldS, newS);
  }

  public static @NotNull String replaceIgnoreCase(@NotNull final String text, @NotNull final String oldS, @NotNull final String newS) {
    return StringUtil.replaceIgnoreCase(text, oldS, newS);
  }

  public static String replace(@NotNull final String text, @NotNull final String oldS,
      @NotNull final String newS, final boolean ignoreCase) {
    return StringUtil.replace(text, oldS, newS, ignoreCase);
  }

  public static int indexOfIgnoreCase(@NotNull final String where,
      @NotNull final String what, final int fromIndex) {
    return StringUtil.indexOfIgnoreCase(where, what, fromIndex);
  }

  public static int indexOfIgnoreCase(@NotNull final CharSequence where,
      @NotNull final CharSequence what, final int fromIndex) {
    return StringUtil.indexOfIgnoreCase(where, what, fromIndex);
  }

  public static int lastIndexOfIgnoreCase(@NotNull final String where,
      final char c, final int fromIndex) {
    return StringUtil.lastIndexOfIgnoreCase(where, c, fromIndex);
  }

  public static boolean containsIgnoreCase(@NotNull final String where,
      @NotNull final String what) {
    return StringUtil.containsIgnoreCase(where, what);
  }

  public static boolean endsWithIgnoreCase(@NotNull final String str, @NotNull final String suffix) {
    return StringUtil.endsWithIgnoreCase(str, suffix);
  }

  public static boolean startsWithIgnoreCase(@NotNull final String str, @NotNull final String prefix) {
    return StringUtil.startsWithIgnoreCase(str, prefix);
  }

  @NotNull
  public static String stripHtml(@NotNull final String html, final boolean convertBreaks) {
    return StringUtil.stripHtml(html, convertBreaks);
  }

  @NotNull
  public static String stripHtml(@NotNull final String html, @Nullable final String breaks) {
    return StringUtil.stripHtml(html, breaks);
  }

  public static String toLowerCase(@Nullable final String str) {
    return StringUtil.toLowerCase(str);
  }

  @NotNull
  public static String getPackageName(@NotNull final String fqName) {
    return StringUtil.getPackageName(fqName);
  }

  @NotNull
  public static String getPackageName(@NotNull final String fqName, final char separator) {
    return StringUtil.getPackageName(fqName, separator);
  }

  public static int getLineBreakCount(@NotNull final CharSequence text) {
    return StringUtil.getLineBreakCount(text);
  }

  public static boolean containsLineBreak(@NotNull final CharSequence text) {
    return StringUtil.containsLineBreak(text);
  }

  public static boolean isLineBreak(final char c) {
    return StringUtil.isLineBreak(c);
  }

  public static @NotNull String escapeLineBreak(@NotNull final String text) {
    return StringUtil.escapeLineBreak(text);
  }

  public static boolean endsWithLineBreak(@NotNull final CharSequence text) {
    return StringUtil.endsWithLineBreak(text);
  }

  public static int lineColToOffset(@NotNull final CharSequence text, final int line, final int col) {
    return StringUtil.lineColToOffset(text, line, col);
  }

  public static int offsetToLineNumber(@NotNull final CharSequence text, final int offset) {
    return StringUtil.offsetToLineNumber(text, offset);
  }

  public static LineColumn offsetToLineColumn(@NotNull final CharSequence text, final int offset) {
    return StringUtil.offsetToLineColumn(text, offset);
  }

  public static int difference(@NotNull final String s1, @NotNull final String s2) {
    return StringUtil.difference(s1, s2);
  }

  @NotNull
  public static String wordsToBeginFromUpperCase(@NotNull final String s) {
    return StringUtil.wordsToBeginFromUpperCase(s);
  }

  @NotNull
  public static String wordsToBeginFromLowerCase(@NotNull final String s) {
    return StringUtil.wordsToBeginFromLowerCase(s);
  }

  @NotNull
  public static String toTitleCase(@NotNull final String s) {
    return StringUtil.toTitleCase(s);
  }

  /**
   * Parse the text representation of an array initializer into an array of strings.
   * <p>
   * Note that this function will ignore the leading and trailing white spaces of
   * the text, and the leading and trailing white spaces of each item, and the empty
   * items.
   *
   * @param text
   *     the text representation of a array to parse, which has the form
   *     of "{item1, item2, item3, ...}".
   * @return
   *     the array of strings parsed from the text.
   * @author Haixing Hu
   */
  public static String[] parseArrayInitializerText(final String text) {
    if (text == null) {
      return new String[0];
    }
    final String trimmed = text.trim();
    if ((trimmed.length() < 2)
        || (trimmed.charAt(0) != '{')
        || (trimmed.charAt(trimmed.length() - 1) != '}')) {
      logger.error("Invalid array initializer text: " + text);
      return new String[0];
    }
    final String[] items = trimmed.substring(1, trimmed.length() - 1).split(",");
    final List<String> result = new ArrayList<>();
    for (int i = 0; i < items.length; ++i) {
      final String item = items[i].trim();
      if (!item.isEmpty()) {
        result.add(item);
      }
    }
    return result.toArray(new String[0]);
  }

  /**
   * Unquote an array of strings.
   *
   * @param array
   *     the array of strings to unquote.
   * @return
   *     the array of strings unquoted.
   * @author Haixing Hu
   */
  public static String[] unquoteStringArray(final String[] array) {
    final String[] result = new String[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = StringUtil.unquoteString(array[i]);
    }
    return result;
  }
}
