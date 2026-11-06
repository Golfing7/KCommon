package com.golfing8.kcommon.idea

import com.intellij.psi.PsiElement

/**
 * Resolves a bare type name - as written in a `#$Type <name>` tag (see [FileTagParser]) - to a
 * [ConfigFieldType], and lists every name available for completion. Five sources are combined,
 * each optional so this degrades gracefully:
 *  - [BuiltInAdapters] - KCommon's own shipped types, always available.
 *  - [UserAdapterRegistry] - a consuming project's own `@ConfigAdapterInfo` adapters, when its
 *    classes are on the classpath.
 *  - [ConfigPsiUtil] CASerializable classes discovered by simple name, likewise classpath-dependent.
 *  - [ConfigPsiUtil] classes extending `ConfigClass` directly (e.g. a `SubModule`), likewise
 *    classpath-dependent - KCommon's original config engine, distinct in shape from CASerializable
 *    (see [ConfigPsiUtil.collectConfigClassFields]'s doc).
 *  - [ExternalSchemaRegistry] - types pulled in via an import, for when the classpath isn't available
 *    (e.g. a lone YAML file opened for remote editing outside its project).
 */
object NamedTypeRegistry {

    /** [context] is where the name is used; it decides the platform. */
    fun resolve(context: PsiElement, name: String): ConfigFieldType? {
        val project = context.project
        BuiltInAdapters.byTypeName(context, name)?.let { return it }
        UserAdapterRegistry.byTypeName(project, name)?.let { return it }
        ConfigPsiUtil.findNamedCASerializable(project, name)?.let { return it }
        ConfigPsiUtil.findNamedConfigClass(project, name)?.let { return it }
        return ExternalSchemaRegistry.getInstance(project).namedType(name)
    }

    fun allNames(context: PsiElement): List<String> {
        val project = context.project
        val names = LinkedHashSet<String>()
        names += BuiltInAdapters.allTypeNames(context)
        names += UserAdapterRegistry.allTypeNames(project)
        names += ConfigPsiUtil.allCASerializableTypeNames(project)
        names += ConfigPsiUtil.allConfigClassTypeNames(project)
        names += ExternalSchemaRegistry.getInstance(project).allTypeNames()
        return names.sorted()
    }
}
