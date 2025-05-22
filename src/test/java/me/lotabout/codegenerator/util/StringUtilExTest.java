package me.lotabout.codegenerator.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static me.lotabout.codegenerator.util.StringUtilEx.parseArrayInitializerText;

public class StringUtilExTest {

  @Test
  void parseArrayInitializerText_NullInput_ReturnsEmptyArray() {
    assertArrayEquals(new String[0], parseArrayInitializerText(null));
  }

  @Test
  void parseArrayInitializerText_EmptyBraces_ReturnsEmptyArray() {
    assertArrayEquals(new String[0], parseArrayInitializerText("{}"));
  }

  @Test
  void parseArrayInitializerText_SingleItem_ReturnsArrayWithOneItem() {
    assertArrayEquals(new String[]{"item1"}, parseArrayInitializerText("{item1}"));
  }

  @Test
  void parseArrayInitializerText_MultipleItems_ReturnsArrayWithMultipleItems() {
    assertArrayEquals(new String[]{"item1", "item2", "item3"},
        parseArrayInitializerText("{item1, item2, item3}"));
  }

  @Test
  void parseArrayInitializerText_ItemsWithSpaces_ReturnsTrimmedItems() {
    assertArrayEquals(new String[]{"item1", "item2", "item3"},
        parseArrayInitializerText("{ item1 , item2 , item3 }"));
  }

  @Test
  void parseArrayInitializerText_InvalidFormat_ReturnsEmptyArray() {
    assertThrows(AssertionError.class, () -> parseArrayInitializerText("{item1, item2, item3"));
  }

  @Test
  void parseArrayInitializerText_EmptyString_ReturnsEmptyArray() {
    assertThrows(AssertionError.class, () -> parseArrayInitializerText(""));
  }
}
