package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTypesUtil

/**
 * Describes where the valid string values of an enum-like config field come from. Kept separate
 * from any single PsiClass since some backing types (Bukkit's PotionEffectType, DynamicEnum
 * subtypes) aren't real compile-time enums - their valid values are the names of public static
 * fields of their own type instead.
 */
sealed class EnumSource {
    data class JavaEnum(val qualifiedName: String) : EnumSource()
    data class StaticFieldsOfOwnType(val qualifiedName: String) : EnumSource()
    data class FixedValues(val values: List<String>) : EnumSource()
    data class Union(val sources: List<EnumSource>) : EnumSource()

    companion object {
        fun resolve(source: EnumSource, project: Project): List<String> = when (source) {
            is JavaEnum -> {
                val psiClass = JavaPsiFacade.getInstance(project)
                    .findClass(source.qualifiedName, GlobalSearchScope.allScope(project))
                psiClass?.fields?.filterIsInstance<PsiEnumConstant>()?.map { it.name } ?: emptyList()
            }
            is StaticFieldsOfOwnType -> {
                val psiClass = JavaPsiFacade.getInstance(project)
                    .findClass(source.qualifiedName, GlobalSearchScope.allScope(project))
                if (psiClass == null) {
                    emptyList()
                } else {
                    psiClass.fields.filter { field ->
                        field.hasModifierProperty(PsiModifier.STATIC) &&
                            field.hasModifierProperty(PsiModifier.PUBLIC) &&
                            run {
                                val fieldClass = PsiTypesUtil.getPsiClass(field.type)
                                fieldClass != null &&
                                    (fieldClass == psiClass || InheritanceUtil.isInheritor(fieldClass, source.qualifiedName))
                            }
                    }.map { it.name }
                }
            }
            is FixedValues -> source.values
            is Union -> source.sources.flatMap { resolve(it, project) }
        }
    }
}
