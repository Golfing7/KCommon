package com.golfing8.kcommon.idea.preview

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MenuPreviewTest {

    @Test
    fun `ui documents resolve spreads, references and aliases`() {
        val sounds = UiDocument.parse("@ButtonsLight = (Activate: \"click.ogg\");")
        val doc = UiDocument.parse(
            """
            ${'$'}Sounds = "Sounds.ui";
            // a comment
            @Border = 12;
            @Padding = 9 + 8;
            @Base = LabelStyle(FontSize: 17, TextColor: #bfcdd5, RenderBold: true);
            @Disabled = LabelStyle(...@Base, TextColor: #797b7c(0.5));
            /* a template, skipped */
            @TextButton = TextButton { @Anchor = Anchor(); Style: (...@Style); };
            @Style = TextButtonStyle(
              Default: (Background: PatchStyle(TexturePath: "Common/Buttons/Primary.png", Border: @Border), LabelStyle: @Disabled),
              Sounds: ${'$'}Sounds.@ButtonsLight,
            );
            """.trimIndent()
        ) { if (it == "Sounds.ui") sounds else null }

        assertEquals(17.0, doc["Padding"])
        assertNull(doc["TextButton"])
        val default = (doc["Style"] as Map<*, *>)["Default"] as Map<*, *>
        assertEquals(12.0, (default["Background"] as Map<*, *>)["Border"])
        val label = default["LabelStyle"] as Map<*, *>
        assertEquals(17.0, label["FontSize"])
        assertEquals("#797b7c(0.5)", label["TextColor"])
        assertEquals(mapOf("Activate" to "click.ogg"), (doc["Style"] as Map<*, *>)["Sounds"])
    }

    private val menu = mapOf(
        "title" to "&eShop",
        "width" to "500",
        "content" to mapOf("layout-mode" to "Top"),
        "elements" to mapOf(
            "items" to mapOf("layout-mode" to "TopScrolling", "flex-weight" to "1"),
            "close" to mapOf("type" to "TextButton", "special" to "true", "anchor" to mapOf("height" to "44"), "text" to "Close"),
        ),
        "templates" to mapOf(
            "item" to mapOf("layout-mode" to "Left", "children" to mapOf("name" to mapOf("type" to "Label", "text" to "{NAME}"))),
        ),
    )

    @Test
    fun `renders the frame, layouts and rows`() {
        val html = HytaleMenuHtml.render(menu, null, HytaleMenuHtml.Options(rows = mapOf("items" to listOf("item")), sampleRows = 2))
        assertTrue("width:500px" in html, "frame width")
        assertTrue("color:#ffff55\">Shop<" in html, "title color code")
        assertTrue("flex:1.0 1 0" in html, "flex weight")
        assertTrue("overflow-y:auto" in html, "scrolling list")
        assertEquals(2, Regex("\\{NAME}").findAll(html).count(), "sample rows")
        assertFalse("Templates (" in html, "the template was placed")
        assertTrue("special" in html && "Close" in html)
        assertTrue("No Hytale assets found" in html)
    }

    @Test
    fun `special elements can be hidden and unplaced templates are listed`() {
        val html = HytaleMenuHtml.render(menu, null, HytaleMenuHtml.Options(showSpecial = false))
        assertFalse(">Close<" in html)
        assertTrue("Templates (" in html)
    }

    @Test
    fun `text formatting and colors`() {
        assertEquals("<span style=\"color:#ffffff\">a </span><span style=\"color:#55ff55;font-weight:700\">b&lt;</span>",
            TextFormat.toHtml("a &a&lb<", "#ffffff"))
        assertEquals("<span style=\"color:#123456\">x</span>", TextFormat.toHtml("<#123456>x", "#ffffff"))
        assertEquals("rgba(0,0,0,0.45)", HytaleMenuHtml.color("#000000(0.45)"))
        assertEquals("LayoutMode", HytaleMenuHtml.pascal("layout-mode"))
    }

    @Test
    fun `ui documents know the type each name was written as`() {
        val doc = UiDocument.parse("@A = TextButtonStyle(Default: ()); @B = @A; @C = (X: 1); @T = Label { };")
        assertEquals("TextButtonStyle", doc.typeOf("B"))
        assertNull(doc.typeOf("C"))
        assertTrue(doc.has("T") && doc["T"] == null)
    }

    @Test
    fun `samples fill placeholders and paths reach the page`() {
        val html = HytaleMenuHtml.render(menu, null, HytaleMenuHtml.Options(rows = mapOf("items" to listOf("item")), samples = mapOf("NAME" to "Iron Sword"), highlight = "elements/close"))
        assertTrue("Iron Sword" in html)
        assertTrue("data-path=\"templates/item/children/name\"" in html)
        assertTrue("kcHighlight('elements/close')" in html)
        assertEquals(mapOf("NAME" to "Iron Sword", "COST" to "5"), MenuRowRegistry.samples("a: 1\n  #@Sample NAME = Iron Sword\n#@Sample COST=5\n"))
    }

    @Test
    fun `bukkit menus place filler, other and special slots`() {
        val bukkit = mapOf(
            "title" to "&aVault", "type" to "HOPPER", "use-filler-item" to "true",
            "other-slots" to mapOf("info" to mapOf("slot" to "0", "type" to "PAPER", "name" to "&eInfo", "lore" to listOf("&7Line"))),
            "special-slots" to mapOf("claim" to mapOf("slot" to mapOf("x" to "3", "y" to "1"), "item" to mapOf("type" to "CHEST", "amount" to "2"))),
        )
        val html = BukkitMenuHtml.render(bukkit, MinecraftAssets.ItemTextures { "data:x" }, HytaleMenuHtml.Options())
        assertEquals(5, Regex("class=\"slot").findAll(html).count())
        assertTrue("data-path=\"other-slots/info\"" in html && "data-path=\"special-slots/claim\"" in html)
        assertEquals(3, Regex("data-path=\"filler-item\"").findAll(html).count())
        assertTrue(">2<" in html && "Vault" in html)
        val hidden = BukkitMenuHtml.render(bukkit, null, HytaleMenuHtml.Options(showSpecial = false))
        assertFalse("special-slots/claim" in hidden)
    }
}
