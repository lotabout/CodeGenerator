package me.lotabout.codegenerator.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameUtilExTest {

  @Test
  public void testCapitalizeAndUnderscore() {
    assertEquals("HELLO_WORLD", NameUtilEx.capitalizeAndUnderscore("helloWorld"));
  }

  @Test
  public void testLowercaseAndUnderscore() {
    assertEquals("hello_world", NameUtilEx.lowercaseAndUnderscore("helloWorld"));
  }
}
