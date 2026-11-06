package com.golfing8.kcommon.idea

import com.golfing8.kcommon.idea.ConfigFieldType.EnumLike
import com.golfing8.kcommon.idea.ConfigFieldType.MapOf
import com.golfing8.kcommon.idea.ConfigFieldType.Nested
import com.golfing8.kcommon.idea.ConfigFieldType.Unknown

/**
 * The config shape of a Hytale `MenuBuilder`, mirroring `MenuBuilder(Map, String)` and the
 * `MenuSchema` whitelist in the Hytale module. Keep the element table in step with `MenuSchema`:
 * the client disconnects players over markup it doesn't know, so the runtime rejects anything not
 * listed there, and this plugin should flag the same keys.
 *
 * Elements are a [Nested] type discriminated by `type:` (defaulting to Group), and nest through
 * `children`. The schema model is eager, so nesting stops after [MAX_DEPTH] levels; anything deeper
 * just isn't checked.
 */
object HytaleMenuShapes {
    const val MENU_TYPE_NAME = "HytaleMenu"
    private const val MAX_DEPTH = 10

    private val bool = EnumLike(EnumSource.FixedValues(listOf("true", "false")))
    private val alignment = enumOf("Start", "Center", "End")
    private val layoutMode = enumOf(
        "Full", "Left", "Center", "Right", "Top", "Middle", "Bottom", "LeftScrolling", "RightScrolling",
        "TopScrolling", "BottomScrolling", "CenterMiddle", "MiddleCenter", "LeftCenterWrap", "LeftWrap"
    )

    private val anchor = tuple("Anchor", "Width", "Height", "Top", "Bottom", "Left", "Right", "Full", "Horizontal", "Vertical", "MinWidth", "MaxWidth")
    private val padding = tuple("Padding", "Top", "Bottom", "Left", "Right", "Full", "Horizontal", "Vertical")
    private val patch = tuple("PatchStyle", "TexturePath", "Border", "HorizontalBorder", "VerticalBorder", "Color")
    /** A `.ui` reference written as a map, `{ref: '@SecondaryButtonStyle', sounds: '@ButtonSounds'}`; the plain `'@Name'` form is a scalar. */
    private val ref = tuple("UiReference", "Ref", "Sounds")
    private val labelStyle = Nested(
        "LabelStyle",
        props(
            "FontName" to Unknown, "FontSize" to Unknown, "HorizontalAlignment" to alignment, "VerticalAlignment" to alignment,
            "LetterSpacing" to Unknown, "MinShrinkTextToFitFontSize" to Unknown, "OutlineColor" to Unknown, "RenderBold" to bool,
            "RenderItalics" to bool, "RenderUnderlined" to bool, "RenderUppercase" to bool, "ShrinkTextToFit" to bool,
            "TextColor" to Unknown, "Wrap" to bool, "WrapMaxLines" to Unknown,
        )
    )

    private val common = props(
        "Anchor" to anchor, "Padding" to padding, "Background" to patch, "FlexWeight" to Unknown, "Visible" to bool,
        "HitTestVisible" to bool, "TooltipText" to Unknown, "Overscroll" to bool, "MaskTexturePath" to Unknown,
        "KeepScrollPosition" to bool, "AutoScrollDown" to bool,
    )

    /** Each element type's own properties, on top of [common]. */
    private val elementTypes: Map<String, Map<String, ConfigFieldType>> = linkedMapOf(
        "Group" to props("LayoutMode" to layoutMode, "ScrollbarStyle" to ref),
        "Label" to props("Style" to labelStyle),
        "Title" to props("Style" to labelStyle),
        "StyledText" to props("LayoutMode" to layoutMode, "FontSize" to Unknown, "Bold" to bool),
        "TextButton" to props("Style" to ref, "Disabled" to bool),
        "Button" to props("Style" to ref, "Disabled" to bool, "LayoutMode" to layoutMode),
        "ItemIcon" to props("ItemId" to Unknown, "ShowItemTooltip" to bool),
        "ItemSlot" to props(
            "ItemId" to Unknown, "Quantity" to Unknown, "ShowQuantity" to bool, "ShowQualityBackground" to bool, "ShowDurabilityBar" to bool
        ),
        "AssetImage" to props("AssetPath" to Unknown, "FallbackTexturePath" to Unknown),
        "ProgressBar" to props(
            "Value" to Unknown, "Bar" to patch, "BarTexturePath" to Unknown,
            "Alignment" to enumOf("Vertical", "Horizontal"), "Direction" to enumOf("Start", "End")
        ),
        "TextField" to props(
            "PlaceholderText" to Unknown, "Style" to ref, "PlaceholderStyle" to ref, "MaxLength" to Unknown,
            "AutoFocus" to bool, "IsReadOnly" to bool
        ),
    )

    val CONTAINERS = setOf("Group", "Button", "AssetImage")
    val TEXT_TYPES = setOf("Label", "TextButton", "Title", "StyledText")
    val ELEMENT_TYPES: Set<String> get() = elementTypes.keys

    /** `elements`/`templates`/`children`: a map of element key to element. */
    private val elementMap: MapOf by lazy {
        var level = MapOf(Unknown)
        repeat(MAX_DEPTH) { level = MapOf(element(level)) }
        level
    }

    val menu: Nested by lazy {
        Nested(
            MENU_TYPE_NAME,
            linkedMapOf(
                "title" to Unknown,
                "width" to Unknown,
                "height" to Unknown,
                "overlay" to bool,
                "frame" to enumOf("decorated", "none"),
                "lifetime" to enumOf("cant-close", "can-dismiss", "can-dismiss-or-close-through-interaction"),
                "elements-per-page" to Unknown,
                // The content Group's own properties; its elements go under `elements`.
                "content" to Nested("Group", common + elementTypes.getValue("Group")),
                "elements" to elementMap,
                "templates" to elementMap,
            )
        )
    }

    private fun element(children: MapOf): Nested {
        val typeField = EnumLike(EnumSource.FixedValues(elementTypes.keys.toList()))
        val variants = LinkedHashMap<String, Map<String, ConfigFieldType>>()
        for ((type, own) in elementTypes) {
            val fields = LinkedHashMap<String, ConfigFieldType>()
            fields["type"] = typeField
            fields["special"] = bool
            if (type in TEXT_TYPES) fields["text"] = Unknown
            if (type in CONTAINERS) fields["children"] = children
            fields += common
            fields += own
            variants[type] = fields
        }
        return Nested("MenuElement", variants.getValue("Group"), "type", variants)
    }

    /** Markup names are PascalCase; the config writes them in kebab case (`LayoutMode` -> `layout-mode`). */
    private fun props(vararg entries: Pair<String, ConfigFieldType>): Map<String, ConfigFieldType> =
        entries.associate { (name, type) -> ConfigPsiUtil.camelToKebab(name) to type }

    private fun tuple(name: String, vararg keys: String) = Nested(name, props(*keys.map { it to Unknown }.toTypedArray()))

    private fun enumOf(vararg values: String) = EnumLike(EnumSource.FixedValues(values.toList()))
}
