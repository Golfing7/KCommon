package com.golfing8.kcommon.idea

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.Messages

/**
 * Imports one or more KCommon config schema files (as produced by [SchemaExportAction]) into
 * [ExternalSchemaRegistry], letting [ConfigSchemaResolver] resolve modules/types even without the
 * exporting project's Java sources/classes on the classpath - the intended use case is a lone YAML
 * file opened for remote editing (e.g. an SFTP/FileZilla-style workflow). [ExternalSchemaRegistry]
 * is IDE-wide, not per-project, so this only needs running once per machine (not once per project,
 * and not again just because a remote-edit tool handed you a different temp file this time) -
 * multiple files may still be selected and imported at once, and it can be run again later to add more.
 */
class SchemaImportAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val descriptor = FileChooserDescriptorFactory.createMultipleFilesNoJarsDescriptor()
            .withTitle("Import KCommon Config Schema")
            .withDescription("Choose one or more exported KCommon schema JSON files")
            .withFileFilter { it.extension.equals("json", ignoreCase = true) }

        val files = FileChooser.chooseFiles(descriptor, project, null)
        if (files.isEmpty()) return

        val registry = ExternalSchemaRegistry.getInstance(project)
        val failures = registry.addFiles(files.map { it.path })
        val importedCount = files.size - failures.size

        if (failures.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "Imported $importedCount schema file(s). Available in every project/window on this machine.",
                "KCommon Schema Imported"
            )
        } else {
            val message = buildString {
                if (importedCount > 0) append("Imported $importedCount schema file(s).\n\n")
                append("Failed to read/parse:\n")
                append(failures.joinToString("\n"))
            }
            Messages.showWarningDialog(project, message, "Some Schema Files Could Not Be Imported")
        }
    }
}
