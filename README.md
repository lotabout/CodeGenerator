# CodeGenerator
An idea-plugin for code generation, support template customization.

// TODO: add demo

As we know, Intellij had provided useful code generators such as constructors,
getter/setters, equals, hashCode, overrides and delegates, etc. And Intellij
allows us to apply customized velocity templates for each generator. But we
cannot add our own generators.

Code Generator is here to help. Two types of generation are supported here
- Members(fields/method) based templates, such as serializers, equals, etc.
- Class based template, such as transformers, converters, etc. Normally new classes are created.

# Installation

1. Search `CodeGenerator` in Idea plugins
2. Download zip from from [Releases](https://github.com/lotabout/CodeGenerator/releases)

To install a plugin from the disk in idea:

1. Open the `Settings/Preferences` dialog box and select `Plugins` on the left pane.
2. On the right pane of the dialog, click the `Install plugin from disk` button.
3. Select the location of the zip file, click OK to continue.
4. Click `Apply` button of the Settings/Preferences dialog.
5. Restart IntelliJ IDEA to activate.

# Usage

1. Go to the `Settings/Preferences > Other Settings > CodeGenerator` to
   create a new generator/template.
2. Right click on your java file, Select `Generate > CodeGenerator > [name of
   your generator]` to run the generator.

According to the settings of your generator, there might be dialogs show up
asking to select members or classes that's required by your generator.

# Thanks to
- [CodeMaker](https://raw.githubusercontent.com/x-hansong/CodeMaker): where
    the idea and part of code comes from.
- [generate-tostring](https://github.com/JetBrains/intellij-community/tree/master/plugins/generate-tostring):
  Offical toString generator. Part of the code comes from it.

