package me.lotabout.codegenerator.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;

import java.util.concurrent.CompletableFuture;

import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.CodeTemplateList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JSplitPane splitPane;
    private JList<TemplateEditPane> templateList;
    private DefaultListModel<TemplateEditPane> templateListModel;
    private JButton deleteTemplateButton;
    private JPanel splitRightPane;
    private JScrollPane scrollPane;
    private JButton importButton;
    private JButton exportButton;
    private JButton exportAllButton;
    private JButton duplicateTemplateButton;

    private static String DEFAULT_EXPORT_PATH = "code-generator.xml";

    public CodeGeneratorConfig(CodeGeneratorSettings settings) {
        this.templateListModel = new DefaultListModel<>();
        this.templateList.setModel(templateListModel);

        templateList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            var length = templateListModel.getSize();
            var index = templateList.getSelectedIndex();
            if (length < 0 || index < 0 || index >= length) {
                splitPane.setRightComponent(splitRightPane);
                deleteTemplateButton.setEnabled(false);
                duplicateTemplateButton.setEnabled(false);
                return;
            }

            var pane = templateListModel.get(templateList.getSelectedIndex());
            deleteTemplateButton.setEnabled(true);
            duplicateTemplateButton.setEnabled(true);
            splitPane.setRightComponent(pane.templateEdit());
        });

        addTemplateButton.addActionListener(e -> {
            var template = new CodeTemplate();
            template.name = "Untitled";
            var editPane = new TemplateEditPane(template);
            var model = (DefaultListModel<TemplateEditPane>) templateList.getModel();
            model.addElement(editPane);
            templateList.setSelectedIndex(model.getSize() - 1);
        });

        deleteTemplateButton.addActionListener(e -> {
            var index = templateList.getSelectedIndex();
            var size = templateListModel.getSize();
            if (index >= 0 && index < size) {
                var result = Messages.showYesNoDialog("Delete this template?", "Delete", null);
                if (result == Messages.OK) {
                    var lastIndex = templateList.getAnchorSelectionIndex();
                    templateListModel.remove(index);

                    var nextIndex = -1;
                    if (lastIndex >= 0 && lastIndex < index || lastIndex == index && index < size - 1) {
                        nextIndex = lastIndex;
                    } else if (lastIndex == index || lastIndex > index && lastIndex < size - 1) {
                        nextIndex = lastIndex - 1;
                    } else if (lastIndex >= index) {
                        nextIndex = size - 2; // should not be here
                    }
                    templateList.setSelectedIndex(nextIndex);
                }
            }
        });

        duplicateTemplateButton.addActionListener(e -> {
            var index = templateList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            var template = templateListModel.get(index);
            var xml = CodeTemplateList.toXML(template.getCodeTemplate());
            var currentTemplates = getTabTemplates();
            var templates = CodeTemplateList.fromXML(xml);
            if (templates == null || templates.isEmpty()) {
                return;
            }

            templates.get(0).name = "Copy of " + templates.get(0).name;
            currentTemplates.addAll(templates);
            refresh(currentTemplates);
            templateList.setSelectedIndex(templateListModel.getSize() - 1);
        });

        exportButton.addActionListener((ActionEvent e) -> {
            var index = templateList.getSelectedIndex();
            var template = templateListModel.get(index);

            var xml = CodeTemplateList.toXML(template.getCodeTemplate());
            saveToFile(xml);
        });

        exportAllButton.addActionListener((ActionEvent e) -> {
            List<CodeTemplate> templates = new ArrayList<>();
            for (var i = 0; i < templateListModel.getSize(); i++) {
                templates.add(templateListModel.get(i).getCodeTemplate());
            }

            var xml = CodeTemplateList.toXML(templates);
            saveToFile(xml);
        });

        importButton.addActionListener(e -> {
            readFromFile().thenAccept(xml -> {
                try {
                    var templates = CodeTemplateList.fromXML(xml);
                    var currentTemplates = getTabTemplates();
                    currentTemplates.addAll(templates);
                    refresh(currentTemplates);
                    Messages.showMessageDialog("Import finished!", "Import", null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog("Fail to import\n" + ex.getMessage(), "Import Error", null);
                }
            });
        });

        resetTabPane(settings.getCodeTemplates());
    }

    public void refresh(List<CodeTemplate> templates) {
        templateListModel.removeAllElements();
        resetTabPane(templates);
    }

    private void saveToFile(String content) {
        final var descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        descriptor.setTitle("Choose Directory to Export");
        descriptor.setDescription("save to directory/" + DEFAULT_EXPORT_PATH + " or the file to overwrite");
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> {
            String targetPath;
            if (virtualFile.isDirectory()) {
                targetPath = virtualFile.getPath() + '/' + DEFAULT_EXPORT_PATH;
            } else {
                targetPath = virtualFile.getPath();
            }

            var path = Paths.get(targetPath);
            if (virtualFile.isDirectory() && Files.exists(path)) {
                var result = Messages.showYesNoDialog("Overwrite the file?\n" + path, "Overwrite", null);
                if (result != Messages.OK) {
                    return;
                }
            }

            try {
                Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Messages.showMessageDialog("Exported to \n" + path, "Export Successful", null);
            } catch (IOException e) {
                e.printStackTrace();
                Messages.showMessageDialog("Error occurred\n" + e.getMessage(), "Export Error", null);
            }
        });
    }

    private CompletableFuture<String> readFromFile() {
        final var descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml");
        descriptor.setTitle("Choose File to Import");
        final var result = new CompletableFuture<String>();
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> result.complete(FileDocumentManager.getInstance().getDocument(virtualFile).getText()));
        return result;
    }

    private void resetTabPane(List<CodeTemplate> templates) {
        templates.forEach(template -> {
            if (template == null) return;
            var editPane = new TemplateEditPane(template);
            templateListModel.addElement(editPane);
        });

        // select first item
        templateList.setSelectedIndex(0);
    }

    public List<CodeTemplate> getTabTemplates() {
        List<CodeTemplate> ret = new ArrayList<>();
        for (var i = 0; i < templateListModel.getSize(); i++) {
            var value = templateListModel.get(i);
            ret.add(value.getCodeTemplate());
        }

        return ret;
    }

    /**
     * Getter method for property <tt>mainPane</tt>.
     *
     * @return property value of mainPane
     */
    public JPanel getMainPane() {
        return mainPane;
    }
}
