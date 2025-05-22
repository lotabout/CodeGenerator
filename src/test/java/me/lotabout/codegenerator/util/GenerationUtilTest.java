package me.lotabout.codegenerator.util;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.jetbrains.java.generate.velocity.VelocityFactory;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenerationUtilTest {

  @Test
  public void testVelocityTools() {
    final ToolManager toolManager = new ToolManager(false, true);
    toolManager.configure(GenerationUtil.VELOCITY_TOOLS_CONFIG);
    final Context context = toolManager.createContext();
    final List<String> names = Arrays.asList("John", "Sarah", "Mark", "Bill");
    context.put("names", names);
    final String template = "#foreach($name in $sorter.sort($names))$name #end";
    final VelocityEngine engine = VelocityFactory.getVelocityEngine();
    final StringWriter sw = new StringWriter();
    engine.evaluate(context, sw, this.getClass().getName(), template);
    System.out.println(sw);
    assertEquals("Bill John Mark Sarah ", sw.toString());
  }

  @Test
  public void testVelocityEvaluate() {
    final Map<String, Object> contextMap = new HashMap<>();
    final String template = """
    #set($names = ["John", "Sarah", "Mark", "Bill"])
    #foreach($name in $sorter.sort($names))$name #end
    """;
    final String result = GenerationUtil.velocityEvaluate(contextMap, null, template, emptyList());
    System.out.println(result);
    assertEquals("\nBill John Mark Sarah ", result);
  }
}
