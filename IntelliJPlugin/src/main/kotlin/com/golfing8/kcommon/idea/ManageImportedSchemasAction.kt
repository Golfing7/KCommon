package com.golfing8.kcommon.idea

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import javax.swing.DefaultListModel
import javax.swing.JComponent

/** Lists the KCommon config schema files currently imported into this project (via [SchemaImportAction]), letting the user remove ones no longer needed. */
class ManageImportedSchemasAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ManageImportedSchemasDialog(project).show()
    }
}

private class ManageImportedSchemasDialog(private val project: Project) : DialogWrapper(project) {

    private val registry = ExternalSchemaRegistry.getInstance(project)
    private val listModel = DefaultListModel<String>().apply {
        registry.importedFilePaths().forEach { addElement(it) }
    }
    private val list = JBList(listModel)

    init {
        title = "Imported KCommon Config Schemas"
        init()
    }

    override fun createCenterPanel(): JComponent {
        list.visibleRowCount = 8
        return ToolbarDecorator.createDecorator(list)
            .disableAddAction()
            .disableUpDownActions()
            .setRemoveAction {
                for (path in list.selectedValuesList) {
                    registry.removeFile(path)
                    listModel.removeElement(path)
                }
            }
            .createPanel()
    }
}
