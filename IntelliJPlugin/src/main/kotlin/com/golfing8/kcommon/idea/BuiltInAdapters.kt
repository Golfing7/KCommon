package com.golfing8.kcommon.idea

import com.golfing8.kcommon.idea.ConfigFieldType.EnumLike
import com.golfing8.kcommon.idea.ConfigFieldType.ListOf
import com.golfing8.kcommon.idea.ConfigFieldType.MapOf
import com.golfing8.kcommon.idea.ConfigFieldType.Nested
import com.golfing8.kcommon.idea.ConfigFieldType.Unknown

/**
 * A hand-authored model of every ConfigAdapter shipped in KCommon itself (mirroring the
 * registrations in ConfigTypeRegistry's static init block), keyed by the FQN of the type each
 * adapter adapts. This gives full-fidelity schema info for KCommon's own types without needing
 * to run any of KCommon's code.
 *
 * Types adapted by generic-root adapters (Enum, List, Set, Map, Optional, CASerializable,
 * DynamicEnum) aren't listed here - they're detected structurally in [ConfigPsiUtil.classifyType]
 * instead, since their shape depends on the specific field's generics/hierarchy, not a fixed FQN.
 * Adapters whose target has no useful structure to validate beyond "it's a scalar" (Range,
 * TimeLength, Duration, World, colors, Interval, MoveLength, Component, ...) are also omitted -
 * they fall through to [ConfigFieldType.Unknown] by default, which is the correct answer for them.
 *
 * Two adapters are deliberately left unmodeled despite being structured (Particle, MenuBuilder):
 * their shape varies per-subtype in ways this table doesn't track, and guessing wrong would cause
 * false "unknown key" warnings on legitimate fields - leniency wins here.
 */
object BuiltInAdapters {

    fun forType(qualifiedName: String): ConfigFieldType? = table[qualifiedName]?.invoke()

    private val table: Map<String, () -> ConfigFieldType> = mapOf(
        "com.golfing8.kcommon.struct.region.Region" to { regionType() },
        "com.golfing8.kcommon.struct.item.ItemStackBuilder" to { itemStackBuilderType() },
        "com.golfing8.kcommon.struct.blocks.WeightedCollection" to { MapOf(Unknown) },
        "org.bukkit.Location" to { locationType() },
        "org.bukkit.util.BlockVector" to { Nested("BlockVector", xyzFields()) },
        "org.bukkit.util.Vector" to { Nested("Vector", xyzFields()) },
        "java.time.ZonedDateTime" to { zonedDateTimeType() },
        "com.golfing8.kcommon.config.lang.Message" to { messageType() },
        "com.golfing8.kcommon.struct.SoundWrapper" to { soundWrapperType() },
        "com.golfing8.kcommon.struct.title.Title" to { titleType() },
        "com.golfing8.kcommon.struct.filter.ItemFilter" to { itemFilterType() },
        "com.golfing8.kcommon.struct.filter.StringFilter" to { stringFilterType() },
        "org.bukkit.potion.PotionEffectType" to { EnumLike(EnumSource.StaticFieldsOfOwnType("org.bukkit.potion.PotionEffectType")) },
        "com.golfing8.kcommon.nms.struct.PotionData" to { potionDataType() },
        "org.bukkit.potion.PotionEffect" to { potionEffectType() },
        "com.golfing8.kcommon.struct.time.Schedule" to { ListOf(Unknown) },
        "com.golfing8.kcommon.nms.struct.EntityData" to { entityDataType() },
        "com.golfing8.kcommon.nms.struct.EntityAttributeModifier" to { entityAttributeModifierType() },
        "com.golfing8.kcommon.struct.drop.Drop" to { dropType() },
        "com.golfing8.kcommon.menu.shape.MenuCoordinate" to { menuCoordinateType() },
        "com.golfing8.kcommon.menu.shape.MenuLayoutShape" to { menuLayoutShapeType() },
        "com.golfing8.kcommon.struct.currency.Currency" to { currencyType() },
        "com.golfing8.kcommon.nms.struct.BookData" to { bookDataType() },
        "org.bukkit.inventory.Recipe" to { recipeType() },
    )

    private fun regionType() = Nested(
        "Region",
        mapOf(
            "region-type" to EnumLike(EnumSource.FixedValues(listOf("CUBOID", "RECTANGLE"))),
            "world" to Unknown,
            "min-x" to Unknown, "max-x" to Unknown,
            "min-y" to Unknown, "max-y" to Unknown,
            "min-z" to Unknown, "max-z" to Unknown,
        )
    )

    private fun xyzFields() = mapOf("x" to Unknown, "y" to Unknown, "z" to Unknown)

    private fun locationType() = Nested(
        "Location",
        mapOf("world" to Unknown, "x" to Unknown, "y" to Unknown, "z" to Unknown, "yaw" to Unknown, "pitch" to Unknown)
    )

    private fun zonedDateTimeType() = Nested(
        "ZonedDateTime",
        mapOf(
            "zone-id" to Unknown, "year" to Unknown, "month" to Unknown,
            "day" to Unknown, "hour" to Unknown, "minute" to Unknown, "second" to Unknown,
        )
    )

    private fun stringFilterType() = Nested(
        "StringFilter",
        mapOf("ignore-case" to Unknown, "contains" to Unknown, "regex" to Unknown, "pattern" to Unknown)
    )

    private fun itemFilterType() = Nested(
        "ItemFilter",
        mapOf(
            "material-filters" to ListOf(stringFilterType()),
            "name-filters" to ListOf(stringFilterType()),
            "lore-filters" to ListOf(stringFilterType()),
            "strip-colors" to Unknown,
        )
    )

    private fun titleType() = Nested(
        "Title",
        mapOf("title" to Unknown, "subtitle" to Unknown, "in" to Unknown, "stay" to Unknown, "out" to Unknown)
    )

    private fun soundWrapperType() = Nested(
        "SoundWrapper",
        mapOf(
            "sound" to EnumLike(EnumSource.JavaEnum("com.cryptomorin.xseries.XSound")),
            "pitch" to Unknown, "volume" to Unknown, "delay" to Unknown,
        )
    )

    private fun messageType() = Nested(
        "Message",
        mapOf(
            "paged" to Unknown,
            "page-header" to Unknown,
            "page-footer" to Unknown,
            "page-height" to Unknown,
            "message" to ListOf(Unknown),
            "sounds" to MapOf(soundWrapperType()),
            "actionbar" to Unknown,
            "title" to titleType(),
        )
    )

    private fun potionDataType() = Nested(
        "PotionData",
        mapOf(
            "potion-type" to EnumLike(EnumSource.JavaEnum("org.bukkit.potion.PotionType")),
            "amplified" to Unknown,
            "extended" to Unknown,
        )
    )

    private fun potionEffectType() = Nested(
        "PotionEffect",
        mapOf(
            "effect-type" to EnumLike(EnumSource.StaticFieldsOfOwnType("org.bukkit.potion.PotionEffectType")),
            "duration" to Unknown, "amplifier" to Unknown, "ambient" to Unknown, "particles" to Unknown,
        )
    )

    private fun entityDataType() = EnumLike(
        EnumSource.Union(
            listOf(
                EnumSource.JavaEnum("org.bukkit.entity.EntityType"),
                EnumSource.FixedValues(listOf("CHARGED_CREEPER", "WITHER_SKELETON", "UNDEAD_HORSE", "SKELETON_HORSE")),
            )
        )
    )

    private fun entityAttributeModifierType() = Nested(
        "EntityAttributeModifier",
        mapOf(
            "operation" to EnumLike(EnumSource.JavaEnum("com.golfing8.kcommon.nms.struct.EntityAttributeModifier.Operation")),
            "amount" to Unknown,
            "name" to Unknown,
            "slot" to EnumLike(EnumSource.JavaEnum("org.bukkit.inventory.EquipmentSlot")),
        )
    )

    private fun dropType() = Nested(
        "Drop",
        mapOf(
            "chance" to Unknown,
            "display-name" to Unknown,
            "max-boost" to Unknown,
            "item" to itemStackBuilderType(),
            "items" to MapOf(itemStackBuilderType()),
            "give-directly" to Unknown,
            "fancy" to Unknown,
            "player-locked" to Unknown,
            "boost-quantity" to Unknown,
            "looting-enabled" to Unknown,
            "fortune-enabled" to Unknown,
            "looting-formula" to Unknown,
            "commands" to ListOf(Unknown),
            "xp" to Unknown,
        )
    )

    internal fun menuCoordinateType() = Nested("MenuCoordinate", mapOf("x" to Unknown, "y" to Unknown))

    internal fun menuLayoutShapeType() = Nested(
        "MenuLayoutShape",
        mapOf(
            "type" to EnumLike(EnumSource.FixedValues(listOf("RECTANGLE", "OUTLINE", "POINTS"))),
            "low-slot" to menuCoordinateType(),
            "high-slot" to menuCoordinateType(),
            "points" to ListOf(menuCoordinateType()),
        )
    )

    private fun currencyType() = Nested(
        "Currency",
        mapOf(
            "type" to EnumLike(EnumSource.JavaEnum("com.golfing8.kcommon.struct.currency.EconomyType")),
            "value" to Unknown,
            "format" to Unknown,
        )
    )

    private fun bookDataType() = Nested(
        "BookData",
        mapOf("author" to Unknown, "title" to Unknown, "pages" to ListOf(Unknown))
    )

    private fun recipeType() = Nested(
        "Recipe",
        mapOf(
            "result" to itemStackBuilderType(),
            "key" to Unknown,
            "ingredients" to Unknown,
            "shape" to ListOf(Unknown),
        )
    )

    internal fun itemStackBuilderType() = Nested(
        "ItemStackBuilder",
        mapOf(
            "type" to EnumLike(EnumSource.JavaEnum("com.golfing8.shade.com.cryptomorin.xseries.XMaterial")),
            "amount" to Unknown,
            "durability" to Unknown,
            "unbreakable" to Unknown,
            "custom-model-data" to Unknown,
            "item-model" to Unknown,
            "name" to Unknown,
            "lore" to ListOf(Unknown),
            "nbt-data" to MapOf(Unknown),
            "components" to MapOf(Unknown),
            "potion-data" to potionDataType(),
            "book-data" to bookDataType(),
            "glowing" to Unknown,
            "skull-texture" to Unknown,
            "variable-amount" to Unknown,
            "item-id" to Unknown,
            "enchantments" to MapOf(Unknown),
            "flags" to ListOf(EnumLike(EnumSource.JavaEnum("org.bukkit.inventory.ItemFlag"))),
            "attribute-modifiers" to MapOf(ListOf(entityAttributeModifierType())),
            "unstackable" to Unknown,
            "color" to Unknown,
        )
    )
}
