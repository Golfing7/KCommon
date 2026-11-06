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
}
