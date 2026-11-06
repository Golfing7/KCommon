package com.golfing8.kcommon.idea

import com.golfing8.kcommon.idea.preview.HytaleUiAssets
import com.golfing8.kcommon.idea.preview.MinecraftAssets
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
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
 * Decides which [KCommonPlatform] a file belongs to from the game library on its module's
 * classpath: Bukkit means Minecraft, the Hytale server means Hytale. A module with both or neither
 * falls back to the whole project's libraries, and when those don't decide either (such as KCommon
 * itself, or a lone YAML file) to the per-project override from Settings | Tools | KCommon, and
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

    fun detect(module: Module): Detected = CachedValuesManager.getManager(module.project).getCachedValue(module) {
        val facade = JavaPsiFacade.getInstance(module.project)
        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        CachedValueProvider.Result.create(
            Detected(facade.findClass(MINECRAFT_MARKER, scope) != null, facade.findClass(HYTALE_MARKER, scope) != null),
            ProjectRootModificationTracker.getInstance(module.project)
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

    /** The platform of the module [context] is in; see the class doc for the fallbacks. */
    fun platform(context: PsiElement): KCommonPlatform {
        val module = ModuleUtilCore.findModuleForPsiElement(context.containingFile?.originalFile ?: context)
        if (module != null) decide(detect(module))?.let { return it }
        return platform(context.project)
    }

    /** The platform for the project as a whole, for anything not tied to one module. */
    fun platform(project: Project): KCommonPlatform = decide(detect(project)) ?: override(project) ?: KCommonPlatform.MINECRAFT

    fun isHytale(context: PsiElement): Boolean = platform(context) == KCommonPlatform.HYTALE

    private fun decide(detected: Detected): KCommonPlatform? = when {
        detected.hytale && !detected.minecraft -> KCommonPlatform.HYTALE
        detected.minecraft && !detected.hytale -> KCommonPlatform.MINECRAFT
        else -> null
    }
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
    private var minecraftJar: String = MinecraftAssets.configuredPath().orEmpty()

    override fun createPanel() = panel {
        row("Platform:") {
            comboBox(Choice.entries).bindItem(::choice)
        }
        row {
            // Detection reads indexes, which can't happen on the UI thread, so the text fills in once it's known.
            val label = comment("Checking libraries...").component
            com.intellij.openapi.application.ReadAction.nonBlocking<String> { detectedText() }
                .finishOnUiThread(com.intellij.openapi.application.ModalityState.any()) { label.text = it }
                .submit(com.intellij.util.concurrency.AppExecutorUtil.getAppExecutorService())
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
        row("Minecraft client jar:") {
            textFieldWithBrowseButton(
                "Minecraft Client Jar",
                project,
                FileChooserDescriptorFactory.createSingleFileDescriptor("jar")
            ).bindText(::minecraftJar).columns(COLUMNS_LARGE)
        }
        row {
            comment("Item textures for previewing Bukkit menus. Left empty, the newest version in the launcher's versions folder is used: " +
                "${MinecraftAssets.defaultPath() ?: "not found"}.")
        }
    }

    override fun apply() {
        super.apply()
        PlatformDetector.setOverride(project, choice?.platform)
        HytaleUiAssets.setConfiguredPath(assetsPath.trim().ifEmpty { null })
        MinecraftAssets.setConfiguredPath(minecraftJar.trim().ifEmpty { null })
    }

    private fun detectedText(): String {
        if (project.isDefault) return "Set for each project."
        if (DumbService.isDumb(project)) return "Libraries are still being indexed."
        val detected = PlatformDetector.detect(project)
        return when {
            detected.hytale && detected.minecraft ->
                "Both Bukkit and the Hytale server are on the classpath. Each module with only one of them still uses that one; this choice covers the rest."
            detected.hytale -> "Detected: Hytale. This choice only applies where a module's libraries don't decide."
            detected.minecraft -> "Detected: Minecraft (Bukkit). This choice only applies where a module's libraries don't decide."
            else -> "Neither Bukkit nor the Hytale server is on the classpath, so this choice decides (Minecraft when left to detection)."
        }
    }
}
