package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTypesUtil

/** Reflection-mirroring PSI helpers: reads @ModuleInfo / @Conf / CASerializable / ConfigClass the same way KCommon's runtime does. */
object ConfigPsiUtil {

    private val MODULES_BY_NAME_KEY: Key<CachedValue<Map<String, PsiClass>>> = Key.create("kcommon.modulesByName")
    private val CA_SERIALIZABLE_CLASSES_KEY: Key<CachedValue<Map<String, PsiClass>>> = Key.create("kcommon.caSerializableClasses")
    private val CONFIG_CLASS_TYPES_KEY: Key<CachedValue<Map<String, PsiClass>>> = Key.create("kcommon.configClassTypes")

    fun findModuleClassByName(project: Project, moduleId: String): PsiClass? =
        allModulesByName(project).entries.firstOrNull { it.key.equals(moduleId, ignoreCase = true) }?.value

    /** Every @ModuleInfo-annotated class in the project, keyed by its declared (original-case) module id - e.g. for [SchemaExportAction] to enumerate every module, or for tag completion to suggest module ids. */
    fun allModulesByName(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(project, MODULES_BY_NAME_KEY, {
            val annotationClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.MODULE_INFO, GlobalSearchScope.allScope(project))

            val result: Map<String, PsiClass> = if (annotationClass == null) {
                emptyMap()
            } else {
                val scope = GlobalSearchScope.allScope(project)
                val map = LinkedHashMap<String, PsiClass>()
                for (candidate in AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope)) {
                    val annotation = candidate.getAnnotation(KCConstants.MODULE_INFO) ?: continue
                    val nameValue = annotation.findAttributeValue("name") ?: continue
                    val constant = JavaPsiFacade.getInstance(project).constantEvaluationHelper
                        .computeConstantExpression(nameValue) as? String ?: continue
                    map[constant] = candidate
                }
                map
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
    }

    /**
     * Every config bucket name referenced anywhere in [moduleClass]'s own @Conf fields/nested
     * ConfigClass children, or any of [configSources]' @Conf fields - always including the default
     * "config" bucket. For [SchemaExportAction] to know which buckets to export per module.
     */
    fun collectBuckets(moduleClass: PsiClass, configSources: List<PsiClass>): Set<String> {
        val buckets = linkedSetOf(KCConstants.MAIN_CONFIG_BUCKET)
        collectBucketNames(moduleClass, includeChildren = true, buckets)
        for (source in configSources) {
            collectBucketNames(source, includeChildren = false, buckets)
        }
        return buckets
    }

    private fun collectBucketNames(psiClass: PsiClass, includeChildren: Boolean, into: MutableSet<String>) {
        var current: PsiClass? = psiClass
        while (current != null) {
            for (field in current.fields) {
                if (!isConfEligible(field)) continue
                into += confBucket(field.getAnnotation(KCConstants.CONF))
            }
            current = nextConfigClassSuperclass(current)
        }
        if (includeChildren) {
            for (inner in configClassChildrenOf(psiClass)) {
                collectBucketNames(inner, includeChildren = true, into)
            }
        }
    }

    /** A CASerializable class anywhere on the project's classpath, found by its simple name - for `#$Type` tag resolution (see [NamedTypeRegistry]). Null for a flattened type (see [isFlattened]), since those have no fixed key set to offer. */
    fun findNamedCASerializable(project: Project, name: String): ConfigFieldType.Nested? {
        val match = allCASerializableClasses(project)[name] ?: return null
        if (isFlattened(match)) return null
        return ConfigFieldType.Nested(match.name ?: name, collectConfFields(match))
    }

    fun allCASerializableTypeNames(project: Project): List<String> = allCASerializableClasses(project).keys.toList()

    private fun allCASerializableClasses(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(project, CA_SERIALIZABLE_CLASSES_KEY, {
            val caClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.CA_SERIALIZABLE, GlobalSearchScope.allScope(project))

            val result: Map<String, PsiClass> = if (caClass == null) {
                emptyMap()
            } else {
                val map = LinkedHashMap<String, PsiClass>()
                for (candidate in ClassInheritorsSearch.search(caClass, GlobalSearchScope.allScope(project), true)) {
                    val name = candidate.name ?: continue
                    map.putIfAbsent(name, candidate)
                }
                map
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
    }

    /**
     * A class extending `com.golfing8.kcommon.config.generator.ConfigClass` directly - KCommon's
     * original config engine, which `Module`/`SubModule` are themselves built on (see
     * [collectConfigClassFields]'s doc) - found anywhere on the project's classpath by its simple
     * name, for `#$Type` tag resolution (see [NamedTypeRegistry]). Unlike a module/configSources
     * lookup, this isn't scoped to any one config bucket - a standalone type reference wants the
     * whole shape, not just whichever fields happen to target a particular file.
     */
    fun findNamedConfigClass(project: Project, name: String): ConfigFieldType.Nested? {
        val match = allConfigClassTypes(project)[name] ?: return null
        return ConfigFieldType.Nested(match.name ?: name, collectConfigClassFields(match, bucket = null, project, includeChildren = true))
    }

    fun allConfigClassTypeNames(project: Project): List<String> = allConfigClassTypes(project).keys.toList()

    private fun allConfigClassTypes(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(project, CONFIG_CLASS_TYPES_KEY, {
            val configClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.CONFIG_CLASS, GlobalSearchScope.allScope(project))

            val result: Map<String, PsiClass> = if (configClass == null) {
                emptyMap()
            } else {
                val map = LinkedHashMap<String, PsiClass>()
                for (candidate in ClassInheritorsSearch.search(configClass, GlobalSearchScope.allScope(project), true)) {
                    val name = candidate.name ?: continue
                    map.putIfAbsent(name, candidate)
                }
                map
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
    }

    fun extractConfigSources(annotation: PsiAnnotation): List<PsiClass> {
        val value = annotation.findAttributeValue("configSources") ?: return emptyList()
        val values = if (value is PsiArrayInitializerMemberValue) value.initializers.toList() else listOf(value)
        return values.mapNotNull { v ->
            (v as? PsiClassObjectAccessExpression)?.operand?.type?.let { PsiTypesUtil.getPsiClass(it) }
        }
    }

    /** Mirrors FieldHandle#shouldSerialize: only non-static/non-transient fields (and never the reserved _key field) are serialized. */
    private fun shouldSerialize(field: PsiField): Boolean {
        if (field.name == KCConstants.KEY_FIELD_NAME) return false
        val modifiers = field.modifierList ?: return true
        return !modifiers.hasModifierProperty(PsiModifier.STATIC) && !modifiers.hasModifierProperty(PsiModifier.TRANSIENT)
    }

    /** Mirrors `ConfigClass#resolveFields` with `requireAnnotation = true` (always the case for a Module/SubModule/ConfigClass-driven field): eligible regardless of static-ness, as long as it isn't transient/final and carries `@Conf`. */
    private fun isConfEligible(field: PsiField): Boolean {
        val modifiers = field.modifierList ?: return false
        if (modifiers.hasModifierProperty(PsiModifier.TRANSIENT) || modifiers.hasModifierProperty(PsiModifier.FINAL)) return false
        return field.getAnnotation(KCConstants.CONF) != null
    }

    private fun confBucket(conf: PsiAnnotation?): String {
        val configAttr = stringLiteralValue(conf?.findAttributeValue("config"))
        return if (configAttr.isNullOrBlank() || configAttr == KCConstants.DEFAULT_CONFIG_BUCKET) KCConstants.MAIN_CONFIG_BUCKET else configAttr
    }

    /**
     * The fields of [psiClass] belonging to [bucket] (e.g. "config", "limits") - or, if [bucket] is
     * null, every `@Conf` field regardless of which bucket it targets (for a standalone `#$Type`
     * reference, where "the whole shape" is wanted, not just one file's slice of it).
     *
     * Mirrors `ConfigClass#resolveFields`/`#initConfig`/`#resolveChildren` (KCommon's original config
     * engine, which `Module`/`SubModule` are themselves thin wrappers around - see
     * `ConfigClassWrapper`): a field is walked up through intermediate `ConfigClass`-extending
     * superclasses (stopping at `ConfigClass` itself), and - only when [includeChildren] is true, i.e.
     * only for whatever plays the role of a `ConfigClass`'s own `self` (a module class, or a
     * standalone type referenced by `#$Type`; real `ConfigClassSource` entries never get this, since
     * KCommon's own `ConfigClass#addSource` never calls `resolveChildren` on them either) - every
     * `static` nested class assignable to `ConfigClass` is recursively resolved into a `Nested` entry,
     * keyed by its own `@Conf.label()` if set, otherwise its raw (not kebab-cased) simple name -
     * exactly matching `ConfigClass#buildPath`'s literal fallback.
     */
    fun collectConfigClassFields(psiClass: PsiClass, bucket: String?, project: Project, includeChildren: Boolean): Map<String, ConfigFieldType> {
        val fields = LinkedHashMap<String, ConfigFieldType>()

        var current: PsiClass? = psiClass
        while (current != null) {
            for (field in current.fields) {
                if (!isConfEligible(field)) continue
                val conf = field.getAnnotation(KCConstants.CONF)
                if (bucket != null && !confBucket(conf).equals(bucket, ignoreCase = true)) continue
                fields.putIfAbsent(yamlKeyFor(field, conf), classifyType(field.type, project))
            }
            current = nextConfigClassSuperclass(current)
        }

        if (includeChildren) {
            for (inner in configClassChildrenOf(psiClass)) {
                val childFields = collectConfigClassFields(inner, bucket, project, includeChildren = true)
                // Bucket-filtering can leave a child with nothing for THIS bucket - only prune it
                // then; an unfiltered (bucket == null) lookup always includes every child's full shape.
                if (bucket != null && childFields.isEmpty()) continue
                fields[configClassChildKey(inner)] = ConfigFieldType.Nested(inner.name ?: "object", childFields)
            }
        }

        return fields
    }

    /** The intermediate superclass to keep walking for more `@Conf` fields, per `ConfigClass#resolveFields`'s own recursion condition - null once we'd hit `ConfigClass` itself (the abstract base has nothing to reflect over) or leave `ConfigClass`'s hierarchy entirely. */
    private fun nextConfigClassSuperclass(clazz: PsiClass): PsiClass? {
        val superClass = clazz.superClass ?: return null
        return if (superClass.qualifiedName != KCConstants.CONFIG_CLASS && InheritanceUtil.isInheritor(superClass, KCConstants.CONFIG_CLASS)) {
            superClass
        } else {
            null
        }
    }

    /** Declared nested classes eligible to be resolved as `ConfigClass` children - `static` only: a non-static inner class has no no-arg constructor for `ConfigClass#resolveChildren`'s reflective instantiation to find, so it would fail at runtime rather than actually working this way. */
    private fun configClassChildrenOf(psiClass: PsiClass): List<PsiClass> =
        psiClass.innerClasses.filter { it.hasModifierProperty(PsiModifier.STATIC) && InheritanceUtil.isInheritor(it, KCConstants.CONFIG_CLASS) }

    private fun configClassChildKey(childClass: PsiClass): String {
        val label = stringLiteralValue(childClass.getAnnotation(KCConstants.CONF)?.findAttributeValue("label"))
        return if (!label.isNullOrBlank()) label else (childClass.name ?: "object")
    }

    /** The CASerializable classes currently being expanded on this thread, to break cycles in [collectConfFields] - see its doc. */
    private val expansionStack = ThreadLocal.withInitial { mutableSetOf<PsiClass>() }

    /**
     * Fields of a nested CASerializable type - no bucket filtering, that only applies at the module's top level.
     *
     * A CASerializable type can reference itself, directly or through a cycle of other CASerializable
     * types (e.g. a "condition" type with a `sub-conditions: List<Condition>` field) - naively
     * recursing through [classifyType] for such a field would call back into this same function for
     * the same [psiClass] before its first call ever returns, overflowing the stack. [expansionStack]
     * detects that reentrancy and, per this plugin's usual leniency, just leaves the field that closes
     * the loop as an empty object (unvalidated) instead of expanding it forever.
     */
    fun collectConfFields(psiClass: PsiClass): Map<String, ConfigFieldType> {
        val stack = expansionStack.get()
        if (!stack.add(psiClass)) return emptyMap()
        try {
            return CachedValuesManager.getCachedValue(psiClass) {
                val project = psiClass.project
                val result = LinkedHashMap<String, ConfigFieldType>()
                for (field in psiClass.fields) {
                    if (!shouldSerialize(field)) continue
                    val conf = field.getAnnotation(KCConstants.CONF)
                    result[yamlKeyFor(field, conf)] = classifyType(field.type, project)
                }
                CachedValueProvider.Result.create(
                    result as Map<String, ConfigFieldType>,
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        } finally {
            stack.remove(psiClass)
        }
    }

    private fun yamlKeyFor(field: PsiField, conf: PsiAnnotation?): String {
        val label = stringLiteralValue(conf?.findAttributeValue("label"))
        return if (!label.isNullOrBlank()) label else camelToKebab(field.name)
    }

    private fun stringLiteralValue(value: PsiAnnotationMemberValue?): String? =
        (value as? PsiLiteralExpression)?.value as? String

    /** Mirrors KCommon's own StringUtil.camelToYaml: a dash only on a lower->upper transition. */
    fun camelToKebab(input: String): String {
        val sb = StringBuilder()
        var lastLower = false
        for (c in input) {
            if (c.isUpperCase() && lastLower) {
                lastLower = false
                sb.append('-').append(c.lowercaseChar())
            } else {
                lastLower = c.isLowerCase()
                sb.append(c.lowercaseChar())
            }
        }
        return sb.toString()
    }

    fun classifyType(type: PsiType, project: Project): ConfigFieldType {
        val psiClass = PsiTypesUtil.getPsiClass(type)

        if (type is PsiClassType && psiClass != null) {
            val qualifiedName = psiClass.qualifiedName

            // Optional<T> is transparent to the YAML shape - it's whatever T is.
            if (qualifiedName == KCConstants.OPTIONAL) {
                val inner = type.parameters.getOrNull(0)
                return inner?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
            }

            // Known-by-FQN types take priority: KCommon's own baked-in adapters, then anything a
            // consuming project has declared via @ConfigAdapterInfo.
            if (qualifiedName != null) {
                BuiltInAdapters.forType(qualifiedName)?.let { return it }
                UserAdapterRegistry.forType(project, qualifiedName)?.let { return it }
            }

            if (qualifiedName == KCConstants.RANGE_MAP || InheritanceUtil.isInheritor(psiClass, KCConstants.RANGE_MAP)) {
                val valueParam = type.parameters.getOrNull(0)
                val inner = valueParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.MapOf(inner)
            }

            if (qualifiedName == "java.util.Map" || InheritanceUtil.isInheritor(psiClass, "java.util.Map")) {
                val keyParam = type.parameters.getOrNull(0)
                val valueParam = type.parameters.getOrNull(1)
                val keyType = keyParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                val inner = valueParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.MapOf(inner, keyType)
            }

            if (isCollectionType(qualifiedName, psiClass)) {
                val param = type.parameters.getOrNull(0)
                val inner = param?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.ListOf(inner)
            }

            if (psiClass.isEnum) {
                return ConfigFieldType.EnumLike(EnumSource.JavaEnum(qualifiedName ?: return ConfigFieldType.Unknown))
            }

            if (InheritanceUtil.isInheritor(psiClass, KCConstants.DYNAMIC_ENUM)) {
                return ConfigFieldType.EnumLike(EnumSource.StaticFieldsOfOwnType(qualifiedName ?: return ConfigFieldType.Unknown))
            }

            if (InheritanceUtil.isInheritor(psiClass, KCConstants.CA_SERIALIZABLE) && !isFlattened(psiClass)) {
                return ConfigFieldType.Nested(psiClass.name ?: "object", collectConfFields(psiClass))
            }
        }

        return ConfigFieldType.Unknown
    }

    private fun isCollectionType(qualifiedName: String?, psiClass: PsiClass): Boolean {
        if (qualifiedName == "java.util.List" || qualifiedName == "java.util.Set" || qualifiedName == "java.util.Collection") {
            return true
        }
        return InheritanceUtil.isInheritor(psiClass, "java.util.Collection")
    }

    /**
     * A CASerializable with @CASerializable.Options(flatten = true) collapses to a bare scalar
     * in YAML rather than a mapping, so we must not treat it as a Nested object with validated keys.
     */
    private fun isFlattened(psiClass: PsiClass): Boolean {
        val options = psiClass.getAnnotation(KCConstants.CA_SERIALIZABLE_OPTIONS) ?: return false
        val flatten = options.findAttributeValue("flatten") ?: return false
        return (flatten as? PsiLiteralExpression)?.value as? Boolean ?: false
    }
}
