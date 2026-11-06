package com.golfing8.kcommon.idea.preview

import com.golfing8.kcommon.idea.ConfigFieldType
import com.golfing8.kcommon.idea.ConfigSchemaResolver
import com.golfing8.kcommon.idea.HytaleMenuShapes
import com.golfing8.kcommon.idea.KCConstants
import com.golfing8.kcommon.idea.SchemaResolution
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
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
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
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
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.Alarm
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import org.cef.browser.CefBrowser
import org.cef.handler.CefLifeSpanHandlerAdapter
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel

/** The "KCommon Menu Preview" tool window: draws the menu the caret is in (Hytale or Bukkit), and redraws it as the YAML changes. */
class MenuPreviewToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MenuPreviewPanel(project)
        Disposer.register(toolWindow.disposable, panel)
        toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(panel.component, null, false))
    }
}

private class MenuPreviewPanel(private val project: Project) : Disposable {
    val component = JPanel(BorderLayout())
    private val status = JBLabel("Open a YAML file with a menu")
    private val showSpecial = JBCheckBox("Special elements", true)
    private val zoom = ComboBox(arrayOf("Fit", "100%", "75%", "50%"))
    /** Every menu in the current file; picking one moves the caret to it. */
    private val menuPicker = ComboBox<String>()
    private var updatingPicker = false
    @Volatile
    private var menusInFile: Map<String, SmartPsiElementPointer<YAMLKeyValue>> = emptyMap()
    /** The menu last shown, by its key path, so it stays up while the caret is elsewhere in the file. */
    @Volatile
    private var shownName: String? = null
    // A JCEF that's missing or unsupported leaves a message instead of the browser.
    private val browser: JBCefBrowser? = runCatching { if (JBCefApp.isSupported()) JBCefBrowser() else null }.getOrNull()
    private val navigate: JBCefJSQuery? = browser?.let { JBCefJSQuery.create(it as JBCefBrowserBase) }
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    /** The YAML text last drawn, so caret moves inside the same menu only move the highlight. */
    private var shown: String? = null
    /** The menu last drawn, for clicks in the preview to navigate within. */
    @Volatile
    private var shownMenu: SmartPsiElementPointer<YAMLKeyValue>? = null
    /** The page last drawn; a page loaded before the browser exists is lost, so it's loaded again once it does. */
    @Volatile
    private var lastHtml: String? = null

    init {
        val toolbar = JPanel(BorderLayout(JBUI.scale(8), 0))
        val controls = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(6), 0))
        controls.add(showSpecial)
        controls.add(zoom)
        controls.add(menuPicker)
        toolbar.add(controls, BorderLayout.WEST)
        toolbar.add(status, BorderLayout.CENTER)
        component.add(toolbar, BorderLayout.NORTH)
        if (browser != null && navigate != null) {
            Disposer.register(this, browser)
            Disposer.register(this, navigate)
            browser.jbCefClient.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
                override fun onAfterCreated(cefBrowser: CefBrowser?) {
                    lastHtml?.let(browser::loadHTML)
                }
            }, browser.cefBrowser)
            navigate.addHandler { path ->
                ApplicationManager.getApplication().invokeLater { navigateTo(path) }
                null
            }
            component.add(browser.component, BorderLayout.CENTER)
        } else {
            component.add(JBLabel("The menu preview needs JCEF, which this IDE runtime doesn't provide."), BorderLayout.CENTER)
        }

        showSpecial.addActionListener { shown = null; schedule() }
        zoom.addActionListener { shown = null; schedule() }
        menuPicker.addActionListener {
            if (updatingPicker) return@addActionListener
            val pointer = menusInFile[menuPicker.selectedItem as? String ?: return@addActionListener] ?: return@addActionListener
            val (file, offset) = ReadAction.compute<Pair<com.intellij.openapi.vfs.VirtualFile, Int>?, RuntimeException> {
                pointer.element?.let { target -> target.containingFile?.virtualFile?.let { it to target.textOffset } }
            } ?: return@addActionListener
            PsiNavigationSupport.getInstance().createNavigatable(project, file, offset).navigate(false)
        }
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
        alarm.addRequest(::refresh, 250)
    }

    private fun refresh() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val document = editor.document
        val offset = editor.caretModel.offset
        val options = HytaleMenuHtml.Options(
            showSpecial = showSpecial.isSelected,
            zoom = when (zoom.selectedItem) {
                "100%" -> 1.0
                "75%" -> 0.75
                "50%" -> 0.5
                else -> null
            },
            navigateScript = navigate?.inject("path"),
        )
        ReadAction.nonBlocking<Preview?> { compute(document, offset, options) }
            .withDocumentsCommitted(project)
            .expireWith(this)
            .coalesceBy(this)
            .finishOnUiThread(ModalityState.any(), ::show)
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private class Preview(
        val name: String,
        val source: String,
        val menu: SmartPsiElementPointer<YAMLKeyValue>,
        val caretPath: String?,
        val menus: Map<String, SmartPsiElementPointer<YAMLKeyValue>>,
        val html: () -> String,
    )

    /**
     * The menu the caret is in; with the caret outside every menu, the one shown last (if it's in
     * this file) or the file's first. Null when the file has no menus, which leaves the last preview up.
     */
    private fun compute(document: Document, offset: Int, base: HytaleMenuHtml.Options): Preview? {
        val file = PsiDocumentManager.getInstance(project).getPsiFile(document) as? YAMLFile ?: return null
        val menus = menusIn(file)
        if (menus.isEmpty()) return null
        val at = file.findElementAt(offset) ?: file.findElementAt(offset - 1)
        val caretKey = PsiTreeUtil.getParentOfType(at, YAMLKeyValue::class.java, false)
        var keyValue = caretKey
        while (keyValue != null && menus.values.none { it === keyValue }) {
            keyValue = PsiTreeUtil.getParentOfType(keyValue, YAMLKeyValue::class.java, true)
        }
        if (keyValue == null) keyValue = shownName?.let(menus::get) ?: menus.values.first()
        val name = menus.entries.first { it.value === keyValue }.key
        val kind = kind(keyValue)
        val menu = keyValue.value as? YAMLMapping ?: return null
        @Suppress("UNCHECKED_CAST")
        val config = plain(menu) as Map<String, Any?>
        val caretPath = caretKey?.let { pathWithin(keyValue, it) }
        val options = base.copy(rows = MenuRowRegistry.rows(project), samples = MenuRowRegistry.samples(project) + MenuRowRegistry.samples(file.text), highlight = caretPath)
        val source = menu.text + options.showSpecial + options.zoom + kind + options.samples
        val pointers = SmartPointerManager.getInstance(project)
        return Preview(name, source, pointers.createSmartPsiElementPointer(keyValue), caretPath, menus.mapValues { pointers.createSmartPsiElementPointer(it.value) }) {
            when (kind) {
                Kind.BUKKIT -> BukkitMenuHtml.render(config, MinecraftAssets.current(), options)
                else -> HytaleMenuHtml.render(config, HytaleUiAssets.current(), options)
            }
        }
    }

    private fun show(preview: Preview?) {
        if (preview == null || browser == null) return
        status.text = ""
        shownMenu = preview.menu
        shownName = preview.name
        menusInFile = preview.menus
        updatingPicker = true
        try {
            val names = preview.menus.keys.toList()
            if ((0 until menuPicker.itemCount).map { menuPicker.getItemAt(it) } != names) {
                menuPicker.model = javax.swing.DefaultComboBoxModel(names.toTypedArray())
            }
            menuPicker.selectedItem = preview.name
        } finally {
            updatingPicker = false
        }
        if (preview.source == shown) {
            // Same menu, so only the caret moved.
            browser.cefBrowser.executeJavaScript("window.kcHighlight&&kcHighlight(${HytaleMenuHtml.jsString(preview.caretPath.orEmpty())})", "", 0)
            return
        }
        shown = preview.source
        // Rendering reads textures, so it runs off the UI thread.
        AppExecutorUtil.getAppExecutorService().execute {
            val html = runCatching(preview.html).getOrElse { "<pre>Couldn't draw the menu: ${HytaleMenuHtml.escape(it.toString())}</pre>" }
            lastHtml = html
            browser.loadHTML(html)
        }
    }

    /** Moves the caret to the YAML of the element clicked in the preview. */
    private fun navigateTo(path: String) {
        val (file, offset) = ReadAction.compute<Pair<com.intellij.openapi.vfs.VirtualFile, Int>?, RuntimeException> {
            val menu = shownMenu?.element ?: return@compute null
            var target: YAMLKeyValue = menu
            for (segment in path.split('/')) {
                target = (target.value as? YAMLMapping)?.getKeyValueByKey(segment) ?: break
            }
            target.containingFile?.virtualFile?.let { it to target.textOffset }
        } ?: return
        PsiNavigationSupport.getInstance().createNavigatable(project, file, offset).navigate(true)
    }

    override fun dispose() = Unit

    enum class Kind { HYTALE, BUKKIT }

    companion object {
        /**
         * A Hytale menu: its schema type says so, or, when no schema does, its value has an `elements`
         * section. A Bukkit menu: typed as Bukkit's MenuBuilder, or it has slots or a filler item.
         */
        fun kind(keyValue: YAMLKeyValue): Kind? {
            val resolution = ConfigSchemaResolver.resolveKey(keyValue).resolution
            val type = (resolution as? SchemaResolution.Resolved)?.type as? ConfigFieldType.Nested
            if (type?.typeName == HytaleMenuShapes.MENU_TYPE_NAME) return Kind.HYTALE
            val mapping = keyValue.value as? YAMLMapping ?: return null
            if (mapping.getKeyValueByKey("elements")?.value is YAMLMapping) return Kind.HYTALE
            if (type?.typeName == "MenuBuilder") return Kind.BUKKIT
            if (listOf("special-slots", "other-slots", "filler-item").any { mapping.getKeyValueByKey(it)?.value is YAMLMapping }) return Kind.BUKKIT
            return null
        }

        /** Every menu in [file] by its key path (`crates.preview-menu`), not looking inside a menu for more. */
        fun menusIn(file: YAMLFile): Map<String, YAMLKeyValue> {
            val result = LinkedHashMap<String, YAMLKeyValue>()
            fun walk(mapping: YAMLMapping?, prefix: String) {
                for (kv in mapping?.keyValues.orEmpty()) {
                    val value = kv.value as? YAMLMapping ?: continue
                    val path = prefix + kv.keyText
                    if (kind(kv) != null) result[path] = kv else walk(value, "$path.")
                }
            }
            for (document in file.documents) walk(document.topLevelValue as? YAMLMapping, "")
            return result
        }

        /** [inner]'s key path below [menu], `/`-separated (`elements/footer/children/close`). */
        fun pathWithin(menu: YAMLKeyValue, inner: YAMLKeyValue): String? {
            val keys = ArrayDeque<String>()
            var current: YAMLKeyValue? = inner
            while (current != null && current !== menu) {
                keys.addFirst(current.keyText)
                current = PsiTreeUtil.getParentOfType(current, YAMLKeyValue::class.java, true)
            }
            return if (current === menu) keys.joinToString("/") else null
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

/** What the project's Java code tells the preview about its menus: rows added to lists, and constant placeholder values. */
object MenuRowRegistry {
    /** Container key -> template keys, from `builder.addRow("items", "item")`. */
    fun rows(project: Project): Map<String, List<String>> = scan(project).first

    private val SAMPLE = Regex("""^\s*#+\s*@Sample\s+([A-Za-z0-9_\-]+)\s*=\s*(.*)$""", RegexOption.MULTILINE)

    /** `#@Sample NAME=Iron Sword` comment lines anywhere in a file, as placeholder samples. */
    fun samples(text: String): Map<String, String> = SAMPLE.findAll(text).associate { it.groupValues[1] to it.groupValues[2].trim() }

    /** Placeholder -> value, from calls like `placeholders("NAME", "Iron Sword")` whose arguments are constants. */
    fun samples(project: Project): Map<String, String> = scan(project).second

    private fun scan(project: Project): Pair<Map<String, List<String>>, Map<String, String>> = CachedValuesManager.getManager(project).getCachedValue(project) {
        val scope = GlobalSearchScope.allScope(project)
        val facade = JavaPsiFacade.getInstance(project)
        val helper = facade.constantEvaluationHelper
        val rows = LinkedHashMap<String, MutableSet<String>>()
        val samples = LinkedHashMap<String, String>()
        val names = setOf("addRow", "placeholders", "specialPlaceholders", "globalPlaceholders")
        // Both platforms name their builder com.golfing8.kcommon.menu.MenuBuilder; only Hytale's has addRow and MenuScope.
        val classes = facade.findClasses(KCConstants.MENU_BUILDER, scope).toList() + listOfNotNull(facade.findClass(KCConstants.MENU_SCOPE, scope))
        for (method in classes.flatMap { it.methods.asList() }.filter { it.name in names }) {
            for (reference in MethodReferencesSearch.search(method, scope, true)) {
                val call = PsiTreeUtil.getParentOfType(reference.element, PsiMethodCallExpression::class.java) ?: continue
                val args = call.argumentList.expressions.map { helper.computeConstantExpression(it) }
                if (method.name == "addRow") {
                    val container = args.getOrNull(0) as? String ?: continue
                    val template = args.getOrNull(1) as? String ?: continue
                    rows.getOrPut(container) { linkedSetOf() }.add(template)
                } else {
                    val pairs = if (method.name == "specialPlaceholders") args.drop(1) else args
                    for (i in 0 until pairs.size - 1 step 2) {
                        val key = pairs[i] as? String ?: continue
                        val value = pairs[i + 1] ?: continue
                        samples.putIfAbsent(key, value.toString())
                    }
                }
            }
        }
        CachedValueProvider.Result.create(rows.mapValues { it.value.toList() } to samples, PsiModificationTracker.MODIFICATION_COUNT)
    }
}
