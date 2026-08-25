package com.golfing8.kcommon.idea

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import java.io.File

/**
 * Holds config schemas imported from JSON files exported elsewhere by [SchemaExportAction] (from
 * the real KCommon-consuming project), letting [ConfigSchemaResolver] and [NamedTypeRegistry]
 * resolve modules/types in a project that doesn't have that module's Java sources/classes on its
 * classpath - e.g. a lone YAML file opened for remote editing (an SFTP/FileZilla-style workflow)
 * outside the project it actually belongs to.
 *
 * Deliberately an **application**-level service, not project-level: the whole point is supporting a
 * file that isn't sitting in a stable, dedicated project at all (a remote-edit tool typically hands
 * IntelliJ a fresh temp path per download, and IntelliJ's own no-project LightEdit mode has no
 * `.idea` folder to persist project state into in the first place). Importing once therefore applies
 * IDE-wide, to every window/project on this machine, rather than needing to be redone per project -
 * the tradeoff is that unrelated projects opened in the same IDE share the same imported set.
 *
 * Only the imported files' paths are persisted; their contents are re-read and re-parsed from disk
 * on [reload] - at IDE startup, and right after an import/removal - so re-exporting over an
 * already-imported path is picked up on the next reload without re-importing.
 * When two imported files both define the same (module, bucket) or type name, the file imported
 * later wins - see [addFiles].
 */
@Service(Service.Level.APP)
@State(name = "KCommonExternalSchemas", storages = [Storage("kcommon-external-schemas.xml")])
class ExternalSchemaRegistry : PersistentStateComponent<ExternalSchemaRegistry.State> {

    class State {
        var importedFiles: MutableList<String> = mutableListOf()
    }

    private var myState = State()
    private val modTracker = SimpleModificationTracker()

    /** path -> that file's own parsed content. A [LinkedHashMap] so import order (and thus override order) is preserved. */
    @Volatile
    private var parsed: LinkedHashMap<String, SchemaExport.Parsed> = LinkedHashMap()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
        reload()
    }

    fun importedFilePaths(): List<String> = myState.importedFiles.toList()

    /** Imports (or re-imports) each of [paths]. Returns the subset that failed to read/parse - those are left un-imported. */
    fun addFiles(paths: Collection<String>): List<String> {
        val failures = mutableListOf<String>()
        for (path in paths) {
            val text = runCatching { File(path).readText() }.getOrNull()
            if (text == null) {
                failures.add(path)
                continue
            }
            parsed.remove(path)
            parsed[path] = SchemaExport.parse(text)
            if (path !in myState.importedFiles) myState.importedFiles.add(path)
        }
        modTracker.incModificationCount()
        return failures
    }

    fun removeFile(path: String) {
        myState.importedFiles.remove(path)
        parsed.remove(path)
        modTracker.incModificationCount()
    }

    /** Re-reads every imported file from disk, dropping any that no longer exist/parse. */
    fun reload() {
        val next = LinkedHashMap<String, SchemaExport.Parsed>()
        for (path in myState.importedFiles) {
            val text = runCatching { File(path).readText() }.getOrNull() ?: continue
            next[path] = SchemaExport.parse(text)
        }
        parsed = next
        modTracker.incModificationCount()
    }

    fun modificationTracker(): ModificationTracker = modTracker

    fun fieldsFor(moduleId: String, bucket: String): Map<String, ConfigFieldType>? {
        var result: Map<String, ConfigFieldType>? = null
        for (schema in parsed.values) {
            val moduleEntry = schema.modules.entries.firstOrNull { it.key.equals(moduleId, ignoreCase = true) } ?: continue
            val bucketFields = moduleEntry.value.entries.firstOrNull { it.key.equals(bucket, ignoreCase = true) }?.value ?: continue
            result = bucketFields
        }
        return result
    }

    fun namedType(name: String): ConfigFieldType? {
        var result: ConfigFieldType? = null
        for (schema in parsed.values) {
            schema.types[name]?.let { result = it }
        }
        return result
    }

    fun allModuleIds(): List<String> = parsed.values.flatMap { it.modules.keys }.distinct().sorted()

    fun allBuckets(moduleId: String): List<String> =
        parsed.values.flatMap { schema ->
            schema.modules.entries.firstOrNull { it.key.equals(moduleId, ignoreCase = true) }?.value?.keys.orEmpty()
        }.distinct().sorted()

    fun allTypeNames(): List<String> = parsed.values.flatMap { it.types.keys }.distinct().sorted()

    companion object {
        /**
         * [project] is accepted (and ignored) purely so every existing call site - which naturally
         * has a `Project` on hand from the PSI/action context it's already working with - doesn't
         * need to change now that this is an application-level service; see the class doc for why.
         */
        fun getInstance(@Suppress("UNUSED_PARAMETER") project: Project): ExternalSchemaRegistry = service()
    }
}
