package com.golfing8.kcommon.idea.preview

import com.golfing8.kcommon.idea.ConfigFieldType
import com.golfing8.kcommon.idea.ConfigSchemaResolver
import com.golfing8.kcommon.idea.HytaleMenuShapes
import com.golfing8.kcommon.idea.KCConstants
import com.golfing8.kcommon.idea.SchemaResolution
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.Alarm
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue
import java.awt.BorderLayout
import org.cef.browser.CefBrowser
import org.cef.handler.CefLifeSpanHandlerAdapter
import javax.swing.JPanel

/** The "KCommon Menu Preview" tool window: draws the Hytale menu the caret is in, and redraws it as the YAML changes. */
class MenuPreviewToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MenuPreviewPanel(project)
        Disposer.register(toolWindow.disposable, panel)
        toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(panel.component, null, false))
    }
}

private class MenuPreviewPanel(private val project: Project) : Disposable {
    val component = JPanel(BorderLayout())
    private val status = JBLabel("Put the caret in a Hytale menu")
    private val showSpecial = JBCheckBox("Show special elements", true)
    private val browser: JBCefBrowser? = if (JBCefApp.isSupported()) JBCefBrowser() else null
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    /** The YAML text last drawn, so caret moves inside the same menu don't reload the page. */
    private var shown: String? = null
    /** The page last drawn; a page loaded before the browser exists is lost, so it's loaded again once it does. */
    @Volatile
    private var lastHtml: String? = null

    init {
        val toolbar = JPanel(BorderLayout(JBUI.scale(8), 0))
        toolbar.add(showSpecial, BorderLayout.WEST)
        toolbar.add(status, BorderLayout.CENTER)
        component.add(toolbar, BorderLayout.NORTH)
        if (browser != null) {
            Disposer.register(this, browser)
            browser.jbCefClient.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
                override fun onAfterCreated(cefBrowser: CefBrowser?) {
                    lastHtml?.let(browser::loadHTML)
                }
            }, browser.cefBrowser)
            component.add(browser.component, BorderLayout.CENTER)
        } else {
            component.add(JBLabel("The menu preview needs JCEF, which this IDE runtime doesn't provide."), BorderLayout.CENTER)
        }

        showSpecial.addActionListener { shown = null; schedule() }
        val multicaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                if (event.editor.project == project) schedule()
            }
        }, this)
        multicaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) = schedule()
        }, this)
        project.messageBus.connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) = schedule()
        })
        schedule()
    }

    private fun schedule() {
        if (browser == null) return
        alarm.cancelAllRequests()
        alarm.addRequest(::refresh, 300)
    }

    private fun refresh() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val document = editor.document
        val offset = editor.caretModel.offset
        val special = showSpecial.isSelected
        ReadAction.nonBlocking<Preview?> { compute(document, offset, special) }
            .withDocumentsCommitted(project)
            .expireWith(this)
            .coalesceBy(this)
            .finishOnUiThread(ModalityState.any(), ::show)
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private class Preview(val name: String, val source: String, val html: () -> String)

    /** Null when the caret isn't in a menu, which leaves the last preview up. */
    private fun compute(document: Document, offset: Int, special: Boolean): Preview? {
        val file = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return null
        val at = file.findElementAt(offset) ?: file.findElementAt(offset - 1) ?: return null
        var keyValue = PsiTreeUtil.getParentOfType(at, YAMLKeyValue::class.java, false)
        while (keyValue != null && !isMenu(keyValue)) {
            keyValue = PsiTreeUtil.getParentOfType(keyValue, YAMLKeyValue::class.java, true)
        }
        val menu = keyValue?.value as? YAMLMapping ?: return null
        @Suppress("UNCHECKED_CAST")
        val config = plain(menu) as Map<String, Any?>
        val rows = MenuRowRegistry.rows(project)
        val source = menu.text + special
        return Preview(keyValue.keyText, source) {
            HytaleMenuHtml.render(config, HytaleUiAssets.current(), HytaleMenuHtml.Options(showSpecial = special, rows = rows))
        }
    }

    private fun show(preview: Preview?) {
        if (preview == null || browser == null) return
        status.text = "Menu: ${preview.name}"
        if (preview.source == shown) return
        shown = preview.source
        // Rendering reads textures, so it runs off the UI thread.
        AppExecutorUtil.getAppExecutorService().execute {
            val html = runCatching(preview.html).getOrElse { "<pre>Couldn't draw the menu: ${HytaleMenuHtml.escape(it.toString())}</pre>" }
            lastHtml = html
            browser.loadHTML(html)
        }
    }

    override fun dispose() = Unit

    companion object {
        /** A key whose schema type is a Hytale menu, or, when no schema says so, whose value has a menu's `elements` section. */
        fun isMenu(keyValue: YAMLKeyValue): Boolean {
            val resolution = ConfigSchemaResolver.resolveKey(keyValue).resolution
            val type = (resolution as? SchemaResolution.Resolved)?.type as? ConfigFieldType.Nested
            if (type?.typeName == HytaleMenuShapes.MENU_TYPE_NAME) return true
            val mapping = keyValue.value as? YAMLMapping ?: return false
            return mapping.getKeyValueByKey("elements")?.value is YAMLMapping
        }

        /** The YAML as the plain maps, lists and strings a config section hands MenuBuilder. */
        fun plain(value: YAMLValue?): Any? = when (value) {
            is YAMLMapping -> value.keyValues.associateTo(LinkedHashMap()) { it.keyText to plain(it.value) }
            is YAMLSequence -> value.items.map { plain(it.value) }
            is YAMLScalar -> value.textValue
            else -> null
        }
    }
}

/** Container key -> template keys from `builder.addRow("items", "item")` calls, so previews can fill lists with sample rows. */
object MenuRowRegistry {
    fun rows(project: Project): Map<String, List<String>> = CachedValuesManager.getManager(project).getCachedValue(project) {
        val scope = GlobalSearchScope.allScope(project)
        val facade = JavaPsiFacade.getInstance(project)
        val rows = LinkedHashMap<String, MutableSet<String>>()
        // Both platforms name their builder com.golfing8.kcommon.menu.MenuBuilder; only Hytale's has addRow.
        for (addRow in facade.findClasses(KCConstants.MENU_BUILDER, scope).flatMap { it.findMethodsByName("addRow", false).asList() }) {
            for (reference in MethodReferencesSearch.search(addRow, scope, true)) {
                val call = PsiTreeUtil.getParentOfType(reference.element, PsiMethodCallExpression::class.java) ?: continue
                val args = call.argumentList.expressions
                if (args.size != 2) continue
                val container = facade.constantEvaluationHelper.computeConstantExpression(args[0]) as? String ?: continue
                val template = facade.constantEvaluationHelper.computeConstantExpression(args[1]) as? String ?: continue
                rows.getOrPut(container) { linkedSetOf() }.add(template)
            }
        }
        CachedValueProvider.Result.create(rows.mapValues { it.value.toList() }, PsiModificationTracker.MODIFICATION_COUNT)
    }
}
