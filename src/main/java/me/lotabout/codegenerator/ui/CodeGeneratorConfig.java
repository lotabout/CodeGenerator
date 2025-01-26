package me.lotabout.codegenerator.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;

import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.CodeTemplateList;

public class CodeGeneratorConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JSplitPane splitPane;
    private JList<TemplateEditPane> templateList;
    private final DefaultListModel<TemplateEditPane> templateListModel;
    private JButton deleteTemplateButton;
    private JPanel splitRightPane;
    private JScrollPane scrollPane;
    private JButton importButton;
    private JButton exportButton;
    private JButton exportAllButton;
    private JButton duplicateTemplateButton;

    private static final String DEFAULT_EXPORT_PATH = "code-generator.xml";

    public CodeGeneratorConfig(final CodeGeneratorSettings settings) {
        this.templateListModel = new DefaultListModel<>();
        this.templateList.setModel(templateListModel);

        templateList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            final var length = templateListModel.getSize();
            final var index = templateList.getSelectedIndex();
            if (length < 0 || index < 0 || index >= length) {
                splitPane.setRightComponent(splitRightPane);
                deleteTemplateButton.setEnabled(false);
                duplicateTemplateButton.setEnabled(false);
                return;
            }

            final var pane = templateListModel.get(templateList.getSelectedIndex());
            deleteTemplateButton.setEnabled(true);
            duplicateTemplateButton.setEnabled(true);
            splitPane.setRightComponent(pane.templateEdit());
        });

        addTemplateButton.addActionListener(e -> {
            final var template = new CodeTemplate();
            template.name = "Untitled";
            final var editPane = new TemplateEditPane(template);
            final var model = (DefaultListModel<TemplateEditPane>) templateList.getModel();
            model.addElement(editPane);
            templateList.setSelectedIndex(model.getSize() - 1);
        });

        deleteTemplateButton.addActionListener(e -> {
            final var selectedIndices = templateList.getSelectedIndices(); // Get all selected indices
            final var size = templateListModel.getSize();
            if (selectedIndices.length > 0) {
                final var result = Messages.showYesNoDialog("Delete selected templates?", "Delete", null);
                if (result == Messages.OK) {
                    // Remove selected items in reverse order to avoid index shifting
                    for (int i = selectedIndices.length - 1; i >= 0; i--) {
                        templateListModel.remove(selectedIndices[i]);
                    }
                    // Update selection logic
                    final var newSize = templateListModel.getSize();
                    if (newSize > 0) {
                        // Attempt to select the first remaining item (if any)
                        templateList.setSelectedIndex(Math.min(selectedIndices[0], newSize - 1));
                    } else {
                        // Clear selection if the list is empty
                        templateList.clearSelection();
                    }
                }
            }
        });

        duplicateTemplateButton.addActionListener(e -> {
            final var index = templateList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            final var template = templateListModel.get(index);
            final var xml = CodeTemplateList.toXML(template.getCodeTemplate());
            final var currentTemplates = getTabTemplates();
            final var templates = CodeTemplateList.fromXML(xml);
            if (templates == null || templates.isEmpty()) {
                return;
            }

            templates.get(0).name = "Copy of " + templates.get(0).name;
            currentTemplates.addAll(templates);
            refresh(currentTemplates);
            templateList.setSelectedIndex(templateListModel.getSize() - 1);
        });

        exportButton.addActionListener((final ActionEvent e) -> {
            final var index = templateList.getSelectedIndex();
            final var template = templateListModel.get(index);

            final var xml = CodeTemplateList.toXML(template.getCodeTemplate());
            saveToFile(xml);
        });

        exportAllButton.addActionListener((final ActionEvent e) -> {
            final List<CodeTemplate> templates = new ArrayList<>();
            for (var i = 0; i < templateListModel.getSize(); i++) {
                templates.add(templateListModel.get(i).getCodeTemplate());
            }

            final var xml = CodeTemplateList.toXML(templates);
            saveToFile(xml);
        });

        importButton.addActionListener(e -> {
            readFromFile().thenAccept(xml -> {
                try {
                    final var templates = CodeTemplateList.fromXML(xml);
                    final var currentTemplates = getTabTemplates();
                    currentTemplates.addAll(templates);
                    refresh(currentTemplates);
                    Messages.showMessageDialog("Import finished!", "Import", null);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog("Fail to import\n" + ex.getMessage(), "Import Error", null);
                }
            });
        });

        resetTabPane(settings.getCodeTemplates());
    }

    public void refresh(final List<CodeTemplate> templates) {
        templateListModel.removeAllElements();
        resetTabPane(templates);
    }

    private void saveToFile(final String content) {
        final var descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        descriptor.setTitle("Choose Directory to Export");
        descriptor.setDescription("save to directory/" + DEFAULT_EXPORT_PATH + " or the file to overwrite");
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> {
            final String targetPath;
            if (virtualFile.isDirectory()) {
                targetPath = virtualFile.getPath() + '/' + DEFAULT_EXPORT_PATH;
            } else {
                targetPath = virtualFile.getPath();
            }

            final var path = Paths.get(targetPath);
            if (virtualFile.isDirectory() && Files.exists(path)) {
                final var result = Messages.showYesNoDialog("Overwrite the file?\n" + path, "Overwrite", null);
                if (result != Messages.OK) {
                    return;
                }
            }

            try {
                Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Messages.showMessageDialog("Exported to \n" + path, "Export Successful", null);
            } catch (final IOException e) {
                e.printStackTrace();
                Messages.showMessageDialog("Error occurred\n" + e.getMessage(), "Export Error", null);
            }
        });
    }

    private CompletableFuture<String> readFromFile() {
        final var descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml");
        descriptor.setTitle("Choose File to Import");
        final var result = new CompletableFuture<String>();
        FileChooser.chooseFile(descriptor, null, mainPane, null,
            virtualFile -> result.complete(FileDocumentManager.getInstance().getDocument(virtualFile).getText()));
        return result;
    }

    private void resetTabPane(final List<CodeTemplate> templates) {
        templates.forEach(template -> {
            if (template == null) return;
            final var editPane = new TemplateEditPane(template);
            templateListModel.addElement(editPane);
        });

        // select first item
        templateList.setSelectedIndex(0);
    }

    public List<CodeTemplate> getTabTemplates() {
        final List<CodeTemplate> ret = new ArrayList<>();
        for (var i = 0; i < templateListModel.getSize(); i++) {
            final var value = templateListModel.get(i);
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
