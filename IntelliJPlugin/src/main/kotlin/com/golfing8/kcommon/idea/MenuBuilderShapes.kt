package com.golfing8.kcommon.idea

/** The kind of menu a @MenuContainerInfo-annotated class declares - mirrors MenuContainerInfo.Type in KCommon. */
enum class MenuContainerKind { NORMAL, PAGED }

/**
 * A hand-authored model of MenuBuilder's config shape - mirroring MenuBuilder(ConfigurationSection)'s
 * manual (non-@Conf) parsing, the same way BuiltInAdapters models the CA* adapters. Used for any
 * config path a project declares via @MenuContainerInfo.
 */
object MenuBuilderShapes {

    fun menuBuilderFields(kind: MenuContainerKind): Map<String, ConfigFieldType> {
        val fields = LinkedHashMap<String, ConfigFieldType>()
        fields["type"] = ConfigFieldType.EnumLike(EnumSource.JavaEnum("com.golfing8.kcommon.menu.MenuShapeType"))
        fields["size"] = ConfigFieldType.Unknown
        fields["title"] = ConfigFieldType.Unknown
        fields["use-filler-item"] = ConfigFieldType.Unknown
        fields["filler-item"] = BuiltInAdapters.itemStackBuilderType()
        fields["filler-shape"] = BuiltInAdapters.menuLayoutShapeType()
        fields["special-slots"] = ConfigFieldType.MapOf(simpleGuiItemType(includeAlwaysShow = true))
        fields["other-slots"] = ConfigFieldType.MapOf(simpleGuiItemType(includeAlwaysShow = false))

        if (kind == MenuContainerKind.PAGED) {
            // PagedMenuContainer reads this directly off the menu's own section; the nav buttons
            // themselves are just reserved keys (previous-page/next-page) within special-slots,
            // with an extra optional always-show flag already included above.
            fields["max-page"] = ConfigFieldType.Unknown
        }

        return fields
    }

    /**
     * SimpleGUIItem(ConfigurationSection): either a nested "item" key plus slot(s), or - if "item"
     * is absent - the ItemStackBuilder fields flattened directly onto this same section. Both
     * forms are unioned here since either is valid and we only ever flag genuinely unknown keys.
     */
    private fun simpleGuiItemType(includeAlwaysShow: Boolean): ConfigFieldType.Nested {
        val itemStackFields = BuiltInAdapters.itemStackBuilderType().fields
        val fields = LinkedHashMap<String, ConfigFieldType>(itemStackFields)
        fields["item"] = BuiltInAdapters.itemStackBuilderType()
        fields["slot"] = BuiltInAdapters.menuCoordinateType()
        fields["slots"] = ConfigFieldType.ListOf(BuiltInAdapters.menuCoordinateType())
        if (includeAlwaysShow) {
            // Only meaningful on previous-page/next-page entries, but harmless to allow anywhere.
            fields["always-show"] = ConfigFieldType.Unknown
        }
        return ConfigFieldType.Nested("SimpleGUIItem", fields)
    }
}
