package com.golfing8.kcommon.idea.preview

import com.intellij.ide.util.PropertiesComponent
import java.io.File
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipFile

/**
 * Item textures for the Bukkit menu preview, read from a Minecraft client jar (set IDE-wide in
 * Settings | Tools | KCommon, or the newest vanilla version in the launcher's `versions` folder).
 * Nothing is bundled with the plugin.
 */
object MinecraftAssets {
    private const val PATH_KEY = "kcommon.minecraft.jar"
    private const val TEXTURES = "assets/minecraft/textures/"

    @Volatile
    private var open: Source? = null

    fun configuredPath(): String? =
        if (com.intellij.openapi.application.ApplicationManager.getApplication() == null) null else PropertiesComponent.getInstance().getValue(PATH_KEY)

    fun setConfiguredPath(path: String?) {
        PropertiesComponent.getInstance().setValue(PATH_KEY, path)
    }

    /** The newest vanilla client jar of the standard launcher, if any. */
    fun defaultPath(): String? {
        val home = System.getProperty("user.home")
        val roots = listOfNotNull(
            System.getenv("APPDATA")?.let { "$it/.minecraft" },
            "$home/.minecraft",
            "$home/Library/Application Support/minecraft",
        )
        return roots.asSequence()
            .flatMap { File(it, "versions").listFiles().orEmpty().asSequence() }
            .mapNotNull { dir -> File(dir, "${dir.name}.jar").takeIf { it.isFile && it.length() > 10_000_000 } }
            .maxByOrNull { it.lastModified() }?.path
    }

    @Synchronized
    fun current(): ItemTextures? {
        val path = configuredPath() ?: defaultPath() ?: return null
        val file = File(path)
        if (!file.isFile) return null
        val existing = open
        if (existing != null && existing.file == file && existing.modified == file.lastModified()) return existing
        runCatching { existing?.zip?.close() }
        return runCatching { Source(file) }.getOrNull().also { open = it }
    }

    /** An item's texture as a `data:` URI, by its material name (`DIAMOND_SWORD`). */
    fun interface ItemTextures {
        fun item(material: String): String?
    }

    private class Source(val file: File) : ItemTextures {
        val modified = file.lastModified()
        val zip = ZipFile(file)
        private val cache = ConcurrentHashMap<String, String>()
        private val missing = ConcurrentHashMap.newKeySet<String>()

        override fun item(material: String): String? {
            val name = material.trim().lowercase()
            cache[name]?.let { return it }
            if (name in missing) return null
            val candidates = listOfNotNull(
                "item/$name",
                // Panes and walls are drawn with their block's texture (a pane's own `_top` is a thin strip).
                name.takeIf { it.endsWith("_pane") }?.let { "block/${it.removeSuffix("_pane")}" },
                name.takeIf { it.endsWith("_wall") }?.let { "block/${it.removeSuffix("_wall")}" },
                "block/$name", "block/${name}_front", "block/${name}_top", "block/${name}_side",
            )
            for (candidate in candidates) {
                val entry = zip.getEntry("$TEXTURES$candidate.png") ?: continue
                val bytes = zip.getInputStream(entry).use { it.readBytes() }
                return ("data:image/png;base64," + Base64.getEncoder().encodeToString(bytes)).also { cache[name] = it }
            }
            missing += name
            return null
        }
    }
}
