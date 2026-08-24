package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project

/**
 * Resolves a bare type name - as written in a `#$Type <name>` tag (see [FileTagParser]) - to a
 * [ConfigFieldType], and lists every name available for completion. Four sources are combined,
 * each optional so this degrades gracefully:
 *  - [BuiltInAdapters] - KCommon's own shipped types, always available.
 *  - [UserAdapterRegistry] - a consuming project's own `@ConfigAdapterInfo` adapters, when its
 *    classes are on the classpath.
 *  - [ConfigPsiUtil] CASerializable classes discovered by simple name, likewise classpath-dependent.
 *  - [ExternalSchemaRegistry] - types pulled in via an import, for when the classpath isn't available
 *    (e.g. a lone YAML file opened for remote editing outside its project).
 */
object NamedTypeRegistry {

    fun resolve(project: Project, name: String): ConfigFieldType? {
        BuiltInAdapters.byTypeName(name)?.let { return it }
        UserAdapterRegistry.byTypeName(project, name)?.let { return it }
        ConfigPsiUtil.findNamedCASerializable(project, name)?.let { return it }
        return ExternalSchemaRegistry.getInstance(project).namedType(name)
    }

    fun allNames(project: Project): List<String> {
        val names = LinkedHashSet<String>()
        names += BuiltInAdapters.allTypeNames()
        names += UserAdapterRegistry.allTypeNames(project)
        names += ConfigPsiUtil.allCASerializableTypeNames(project)
        names += ExternalSchemaRegistry.getInstance(project).allTypeNames()
        return names.sorted()
    }
}
