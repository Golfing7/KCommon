package com.golfing8.kcommon.idea

import com.golfing8.kcommon.idea.preview.HytaleUiAssets
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

/** The game a project's KCommon modules run on. The two ship classes with the same names but different config shapes (Message, MenuBuilder, Drop, ...). */
enum class KCommonPlatform { MINECRAFT, HYTALE }

/**
 * Decides a project's [KCommonPlatform] from the game library on its classpath: Bukkit means
 * Minecraft, the Hytale server means Hytale. A project with both (such as KCommon itself) or with
 * neither (a lone YAML file) uses the per-project override from Settings | Tools | KCommon, and
 * Minecraft when none is set.
 */
object PlatformDetector {
    private const val OVERRIDE_KEY = "kcommon.platform.override"
    private const val MINECRAFT_MARKER = "org.bukkit.Bukkit"
    private const val HYTALE_MARKER = "com.hypixel.hytale.server.core.plugin.JavaPlugin"

    data class Detected(val minecraft: Boolean, val hytale: Boolean)

    fun detect(project: Project): Detected = CachedValuesManager.getManager(project).getCachedValue(project) {
        val facade = JavaPsiFacade.getInstance(project)
        val scope = GlobalSearchScope.allScope(project)
        CachedValueProvider.Result.create(
            Detected(facade.findClass(MINECRAFT_MARKER, scope) != null, facade.findClass(HYTALE_MARKER, scope) != null),
            ProjectRootModificationTracker.getInstance(project)
        )
    }

    fun override(project: Project): KCommonPlatform? =
        PropertiesComponent.getInstance(project).getValue(OVERRIDE_KEY)?.let { name -> KCommonPlatform.entries.firstOrNull { it.name == name } }

    fun setOverride(project: Project, platform: KCommonPlatform?) {
        if (override(project) == platform) return
        PropertiesComponent.getInstance(project).setValue(OVERRIDE_KEY, platform?.name)
        // Every schema is cached against PSI modification, so this drops them all.
        PsiManager.getInstance(project).dropPsiCaches()
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    fun platform(project: Project): KCommonPlatform {
        override(project)?.let { return it }
        val detected = detect(project)
        return if (detected.hytale && !detected.minecraft) KCommonPlatform.HYTALE else KCommonPlatform.MINECRAFT
    }

    fun isHytale(project: Project): Boolean = platform(project) == KCommonPlatform.HYTALE
}

/** Settings | Tools | KCommon: the platform override and where the Hytale assets live for menu previews. */
class KCommonSettingsConfigurable(private val project: Project) : BoundConfigurable("KCommon") {

    private enum class Choice(val platform: KCommonPlatform?, private val label: String) {
        AUTO(null, "Detect from libraries"),
        MINECRAFT(KCommonPlatform.MINECRAFT, "Minecraft (Bukkit)"),
        HYTALE(KCommonPlatform.HYTALE, "Hytale");

        override fun toString() = label
    }

    private var choice: Choice? = Choice.entries.first { it.platform == PlatformDetector.override(project) }
    private var assetsPath: String = HytaleUiAssets.configuredPath().orEmpty()

    override fun createPanel() = panel {
        row("Platform:") {
            comboBox(Choice.entries).bindItem(::choice)
        }
        row {
            comment(detectedText())
        }
        row("Hytale assets:") {
            textFieldWithBrowseButton(
                "Hytale Assets.zip or Assets Folder",
                project,
                FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
            ).bindText(::assetsPath).columns(COLUMNS_LARGE)
        }
        row {
            comment("Assets.zip from the Hytale install, or an unpacked HytaleAssets folder. The menu preview reads textures and Common.ui styles from it. " +
                "Left empty, the standard launcher install is used: ${HytaleUiAssets.defaultPath() ?: "not found"}.")
        }
    }

    override fun apply() {
        super.apply()
        PlatformDetector.setOverride(project, choice?.platform)
        HytaleUiAssets.setConfiguredPath(assetsPath.trim().ifEmpty { null })
    }

    private fun detectedText(): String {
        if (project.isDefault) return "Set for each project."
        if (DumbService.isDumb(project)) return "Libraries are still being indexed."
        val detected = PlatformDetector.detect(project)
        return when {
            detected.hytale && detected.minecraft -> "Both Bukkit and the Hytale server are on the classpath; detection picks Minecraft, so choose the platform here."
            detected.hytale -> "Detected: Hytale."
            detected.minecraft -> "Detected: Minecraft (Bukkit)."
            else -> "Neither Bukkit nor the Hytale server is on the classpath; detection picks Minecraft."
        }
    }
}
