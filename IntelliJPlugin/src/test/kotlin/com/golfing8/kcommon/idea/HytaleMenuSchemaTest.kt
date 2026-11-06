package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HytaleMenuSchemaTest {

    private val schema = ConfigSchema("shop") { mapOf("shop-menu" to HytaleMenuShapes.menu(null)) }

    /** Answers the `type:` of the mapping at each path index, as the resolver reads it from YAML. */
    private fun types(vararg byIndex: Pair<Int, String>) = VariantLookup { index, key -> if (key == "type") byIndex.toMap()[index] else null }

    private fun resolved(path: String, variants: VariantLookup = VariantLookup.NONE) =
        (schema.resolve(path.split('.'), variants) as SchemaResolution.Resolved).type

    @Test
    fun `element keys depend on the element's own type`() {
        val label = resolved("shop-menu.elements.name.style", types(2 to "Label")) as ConfigFieldType.Nested
        assertEquals("LabelStyle", label.typeName)
        val button = resolved("shop-menu.elements.name.style", types(2 to "TextButton")) as ConfigFieldType.Nested
        assertEquals("TextButtonStyle", button.typeName)

        // A Group (the default) has no text, a Label has no children.
        assertEquals(SchemaResolution.UnknownKey(parentConfident = true), schema.resolve("shop-menu.elements.box.text".split('.')))
        assertEquals(
            SchemaResolution.UnknownKey(parentConfident = true),
            schema.resolve("shop-menu.elements.name.children".split('.'), types(2 to "Label"))
        )
    }

    @Test
    fun `children nest and keep their own types`() {
        val variants = types(2 to "Group", 4 to "ItemSlot")
        assertEquals(ConfigFieldType.EnumLike(EnumSource.HytaleItemIds), resolved("shop-menu.templates.row.children.icon.item-id", variants))
        val layout = resolved("shop-menu.templates.row.layout-mode") as ConfigFieldType.EnumLike
        assertTrue("TopScrolling" in (layout.source as EnumSource.FixedValues).values)
    }

    @Test
    fun `container completion offers the variant's keys`() {
        val fields = schema.fieldsAt(listOf("shop-menu", "elements", "close"), types(2 to "TextButton"))!!.fields
        assertTrue("text" in fields && "disabled" in fields && "style" in fields)
        assertTrue("layout-mode" !in fields)
    }

    @Test
    fun `menu variants survive a schema export and import`() {
        val json = SchemaExport.write(listOf(SchemaExport.Module("shop", mapOf("menus" to mapOf("shop-menu" to HytaleMenuShapes.menu(null))))), emptyMap(), FakeProject)
        val imported = SchemaExport.parse(json).modules.getValue("shop").getValue("menus")
        val reimported = ConfigSchema("shop") { imported }
        val style = (reimported.resolve("shop-menu.elements.name.style".split('.'), types(2 to "Label")) as SchemaResolution.Resolved).type
        assertEquals("LabelStyle", (style as ConfigFieldType.Nested).typeName)
    }
}
