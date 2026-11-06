package com.golfing8.kcommon.idea.preview

import com.intellij.ide.util.PropertiesComponent
import java.io.File
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipFile

/** What the menu renderer needs from the game's assets; null everywhere when none are available. */
interface MenuAssets {
    /** A UI texture as a `data:` URI, plus the scale it was drawn at (2 for `@2x` files). */
    fun texture(path: String): Texture?

    /** Any image under the assets (an AssetImage path or an item icon), as a `data:` URI. */
    fun image(path: String): String?

    /** The resolved value of a `.ui` reference such as `@DefaultTextButtonStyle` or `Pages/Foo.ui@Bar`. */
    fun reference(ref: String): Any?

    /** Whether a reference points at something, so a typo can be flagged. */
    fun referenceStatus(ref: String): RefStatus = RefStatus.OK

    /** The names a document (`Common.ui` when null) defines, with the `.ui` type each was written as, where known. */
    fun referenceNames(document: String? = null): Map<String, String?> = emptyMap()

    /** The vanilla `.ui` documents a reference can name, such as `Pages/BarterPage.ui`. */
    fun documents(): List<String> = emptyList()

    /** Every item id, sorted. */
    fun itemIds(): List<String> = emptyList()

    /** An item's icon as a `data:` URI. */
    fun itemIcon(id: String): String? = null

    data class Texture(val uri: String, val scale: Int)

    enum class RefStatus { OK, NO_DOCUMENT, NO_VALUE }
}

/**
 * The Hytale assets the menu preview draws with: the `Assets.zip` of a Hytale install, or an
 * unpacked asset folder (both hold `Common/UI/Custom/...`, the root custom UI texture paths are
 * relative to). The path is set IDE-wide in Settings | Tools | KCommon; without one, the launcher's
 * standard install location is used. Nothing is bundled with the plugin.
 */
object HytaleUiAssets {
    private const val PATH_KEY = "kcommon.hytale.assets"
    private const val UI_ROOT = "Common/UI/Custom/"
    private const val INSTALL = "Hytale/install/release/package/game/latest/Assets.zip"
    private const val ITEMS = "Server/Item/Items/"
    /** The first `Icon`/`Parent` in an item file is its own; later ones belong to nested interactions. */
    private val ICON = Regex("\"Icon\"\\s*:\\s*\"([^\"]+)\"")
    private val PARENT = Regex("\"Parent\"\\s*:\\s*\"([^\"]+)\"")

    @Volatile
    private var open: Source? = null

    fun configuredPath(): String? =
        if (com.intellij.openapi.application.ApplicationManager.getApplication() == null) null else PropertiesComponent.getInstance().getValue(PATH_KEY)

    fun setConfiguredPath(path: String?) {
        PropertiesComponent.getInstance().setValue(PATH_KEY, path)
    }

    fun defaultPath(): String? {
        val home = System.getProperty("user.home")
        val candidates = listOfNotNull(
            System.getenv("APPDATA")?.let { "$it/$INSTALL" },
            "$home/.local/share/$INSTALL",
            "$home/.var/app/com.hypixel.HytaleLauncher/data/$INSTALL",
            "$home/Library/Application Support/$INSTALL",
        )
        return candidates.firstOrNull { File(it).isFile }
    }

    /** The assets to draw with, or null when none are configured or found. */
    @Synchronized
    fun current(): MenuAssets? {
        val path = configuredPath() ?: defaultPath() ?: return null
        val file = File(path)
        if (!file.exists()) return null
        val existing = open
        if (existing != null && existing.file == file && existing.modified == file.lastModified()) return existing
        existing?.close()
        return runCatching { Source(file) }.getOrNull().also { open = it }
    }

    private class Source(val file: File) : MenuAssets {
        val modified = file.lastModified()
        private val zip: ZipFile? = if (file.isFile) ZipFile(file) else null
        private val images = ConcurrentHashMap<String, String>()
        private val documents = ConcurrentHashMap<String, UiDocument>()

        fun bytes(path: String): ByteArray? {
            val clean = path.trimStart('/')
            if (zip != null) {
                val entry = zip.getEntry(clean) ?: return null
                return zip.getInputStream(entry).use { it.readBytes() }
            }
            return File(file, clean).takeIf { it.isFile }?.readBytes()
        }

        private fun dataUri(path: String): String? {
            images[path]?.let { return it }
            val bytes = bytes(path) ?: return null
            return ("data:image/png;base64," + Base64.getEncoder().encodeToString(bytes)).also { images[path] = it }
        }

        override fun texture(path: String): MenuAssets.Texture? {
            val relative = UI_ROOT + path.trimStart('/')
            // The client prefers the @2x file; textures are drawn at half their pixel size.
            dataUri(relative.removeSuffix(".png") + "@2x.png")?.let { return MenuAssets.Texture(it, 2) }
            return dataUri(relative)?.let { MenuAssets.Texture(it, 1) }
        }

        override fun image(path: String): String? {
            val clean = path.trimStart('/')
            return listOf(clean, "Common/$clean", UI_ROOT + clean).firstNotNullOfOrNull(::dataUri)
        }

        override fun reference(ref: String): Any? {
            val at = ref.lastIndexOf('@')
            if (at < 0) return null
            val document = ref.substring(0, at).ifEmpty { "Common.ui" }
            return document(document)?.get(ref.substring(at + 1))
        }

        override fun referenceStatus(ref: String): MenuAssets.RefStatus {
            val at = ref.lastIndexOf('@')
            val document = document(ref.substring(0, at.coerceAtLeast(0)).ifEmpty { "Common.ui" }) ?: return MenuAssets.RefStatus.NO_DOCUMENT
            return if (document.has(ref.substring(at + 1))) MenuAssets.RefStatus.OK else MenuAssets.RefStatus.NO_VALUE
        }

        override fun referenceNames(document: String?): Map<String, String?> {
            val doc = document(document ?: "Common.ui") ?: return emptyMap()
            return doc.names.associateWith { doc.typeOf(it) }
        }

        override fun documents(): List<String> = documentList

        private val documentList: List<String> by lazy {
            entries(UI_ROOT).filter { it.endsWith(".ui") && it.count { c -> c == '/' } > UI_ROOT.count { c -> c == '/' } }
                .map { it.removePrefix(UI_ROOT) }.sorted()
        }

        /** Item id -> its definition's top-level `Icon` and `Parent`, from `Server/Item/Items/**/<id>.json`. */
        private val items: Map<String, Pair<String?, String?>> by lazy {
            val result = HashMap<String, Pair<String?, String?>>()
            for (path in entries(ITEMS)) {
                if (!path.endsWith(".json")) continue
                val text = bytes(path)?.toString(Charsets.UTF_8) ?: continue
                result[path.substringAfterLast('/').removeSuffix(".json")] =
                    ICON.find(text)?.groupValues?.get(1) to PARENT.find(text)?.groupValues?.get(1)
            }
            result
        }

        private val sortedIds: List<String> by lazy { items.keys.sorted() }

        override fun itemIds(): List<String> = sortedIds

        override fun itemIcon(id: String): String? {
            var current: String? = id
            var depth = 0
            while (current != null && depth++ < 8) {
                val (icon, parent) = items[current] ?: break
                if (icon != null) return image(icon)
                current = parent
            }
            return image("Icons/ItemsGenerated/$id.png")
        }

        /** Every file path under [prefix]. */
        private fun entries(prefix: String): List<String> {
            if (zip != null) return zip.entries().asSequence().map { it.name }.filter { it.startsWith(prefix) && !it.endsWith("/") }.toList()
            val root = File(file, prefix)
            if (!root.isDirectory) return emptyList()
            return root.walkTopDown().filter { it.isFile }.map { prefix + it.relativeTo(root).invariantSeparatorsPath }.toList()
        }

        private fun document(path: String): UiDocument? {
            documents[path]?.let { return it }
            val text = bytes(UI_ROOT + path)?.toString(Charsets.UTF_8) ?: return null
            // Aliases such as `$Sounds = "Sounds.ui"` are relative to the document; every custom UI document sits under UI_ROOT.
            return UiDocument.parse(text) { document(it) }.also { documents[path] = it }
        }

        fun close() {
            runCatching { zip?.close() }
        }
    }
}
