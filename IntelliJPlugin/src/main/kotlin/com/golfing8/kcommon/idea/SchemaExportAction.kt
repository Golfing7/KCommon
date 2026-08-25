package com.golfing8.kcommon.idea

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Exports every KCommon module (and its config buckets) discoverable in this project's PSI - built
 * the same way [ConfigSchemaResolver] builds a live `config.yml`'s schema - into one JSON file.
 * That file is importable elsewhere via [SchemaImportAction], letting a lone YAML file opened
 * outside this project (e.g. downloaded for remote/SFTP editing) still get full completion and
 * validation without this project's Java sources/classes on its classpath.
 */
class SchemaExportAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val modules = ConfigPsiUtil.allModulesByName(project).map { (moduleId, moduleClass) ->
            val moduleInfo = moduleClass.getAnnotation(KCConstants.MODULE_INFO)
            val configSources = moduleInfo?.let { ConfigPsiUtil.extractConfigSources(it) }.orEmpty()
            val buckets = LinkedHashMap<String, Map<String, ConfigFieldType>>()
            for (bucket in ConfigPsiUtil.collectBuckets(moduleClass, configSources)) {
                buckets[bucket] = ConfigSchemaResolver.buildFieldsFromPsi(project, moduleClass, configSources, bucket)
            }
            SchemaExport.Module(moduleId, buckets)
        }

        if (modules.isEmpty()) {
            Messages.showWarningDialog(
                project,
                "No @ModuleInfo-annotated classes were found in this project - nothing to export.",
                "Nothing to Export"
            )
            return
        }

        val namedTypes = LinkedHashMap<String, ConfigFieldType>()
        for (name in ConfigPsiUtil.allCASerializableTypeNames(project)) {
            ConfigPsiUtil.findNamedCASerializable(project, name)?.let { namedTypes[name] = it }
        }
        for (name in ConfigPsiUtil.allConfigClassTypeNames(project)) {
            ConfigPsiUtil.findNamedConfigClass(project, name)?.let { namedTypes.putIfAbsent(name, it) }
        }

        val json = SchemaExport.write(modules, namedTypes, project)

        val descriptor = FileSaverDescriptor(
            "Export KCommon Config Schema",
            "Choose where to save the exported schema - import it elsewhere via 'Import KCommon Config Schema'",
            "json"
        )
        val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
        val baseDir = project.basePath?.let { LocalFileSystem.getInstance().findFileByPath(it) }
        val destination = dialog.save(baseDir, "kcommon-schema.json") ?: return

        runCatching { destination.file.writeText(json) }
            .onSuccess {
                Messages.showInfoMessage(
                    project,
                    "Exported ${modules.size} module(s) to ${destination.file.path}",
                    "KCommon Schema Exported"
                )
            }
            .onFailure {
                Messages.showErrorDialog(project, "Failed to write export file: ${it.message}", "Export Failed")
            }
    }
}
