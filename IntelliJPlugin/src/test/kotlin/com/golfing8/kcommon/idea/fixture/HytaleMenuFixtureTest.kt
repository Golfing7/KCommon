package com.golfing8.kcommon.idea.fixture

import com.golfing8.kcommon.idea.HytaleMenuBindingInspection
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.jetbrains.yaml.psi.YAMLKeyValue

/**
 * Runs the plugin against a small Hytale-style project in a real (light) IDE: stub KCommon and
 * Hytale classes, a module that loads `shop-menu` from `menus.yml`, and that YAML.
 */
class HytaleMenuFixtureTest : LightJavaCodeInsightFixtureTestCase() {

    override fun setUp() {
        super.setUp()
        stub("com/hypixel/hytale/server/core/plugin/JavaPlugin.java", "package com.hypixel.hytale.server.core.plugin; public class JavaPlugin {}")
        stub("com/golfing8/kcommon/module/ModuleInfo.java",
            "package com.golfing8.kcommon.module; public @interface ModuleInfo { String name(); Class<?>[] configSources() default {}; Class<?>[] langSources() default {}; }")
        stub("com/golfing8/kcommon/config/KSection.java",
            "package com.golfing8.kcommon.config; public interface KSection { default <T> java.util.Optional<T> getOrLoad(String path, Class<T> type) { return null; } }")
        stub("com/golfing8/kcommon/config/lang/LangConf.java", "package com.golfing8.kcommon.config.lang; public @interface LangConf { String path() default \"\"; }")
        stub("com/golfing8/kcommon/config/lang/Message.java", "package com.golfing8.kcommon.config.lang; public class Message {}")
        stub("com/golfing8/kcommon/menu/MenuScope.java", """
            package com.golfing8.kcommon.menu;
            public abstract class MenuScope<S extends MenuScope<S>> {
                public S bindTo(String key, Runnable action) { return null; }
                public S show(String key) { return null; }
                public S text(String key, String text) { return null; }
                public S set(String key, String property, Object value) { return null; }
                public S placeholders(Object... placeholders) { return null; }
            }
        """)
        stub("com/golfing8/kcommon/menu/MenuRow.java", "package com.golfing8.kcommon.menu; public final class MenuRow extends MenuScope<MenuRow> {}")
        stub("com/golfing8/kcommon/menu/MenuBuilder.java",
            "package com.golfing8.kcommon.menu; public class MenuBuilder extends MenuScope<MenuBuilder> { public MenuRow addRow(String container, String template) { return null; } }")
        stub("demo/ShopLang.java", "package demo; public enum ShopLang { NOT_ENOUGH_COINS, BOUGHT }")
        stub("demo/ShopModule.java", """
            package demo;
            import com.golfing8.kcommon.config.KSection;
            import com.golfing8.kcommon.config.lang.LangConf;
            import com.golfing8.kcommon.config.lang.Message;
            import com.golfing8.kcommon.menu.MenuBuilder;
            import com.golfing8.kcommon.module.ModuleInfo;

            @ModuleInfo(name = "shop", langSources = ShopLang.class)
            public class ShopModule {
                @LangConf
                private Message closedMessage;

                KSection getConfig(String name) { return null; }

                void open() {
                    MenuBuilder builder = getConfig("menus").getOrLoad("shop-menu", MenuBuilder.class).orElseThrow();
                    builder.addRow("items", "item").bindTo("buy", () -> {});
                    builder.bindTo("close", () -> {});
                }
            }
        """)
    }

    private fun stub(path: String, text: String) {
        myFixture.addFileToProject(path, text.trimIndent())
    }

    /** Adds a file and opens it with the caret where `<caret>` was. */
    private fun open(path: String, text: String): PsiFile {
        val clean = text.trimIndent()
        val file = myFixture.addFileToProject(path, clean.replace("<caret>", ""))
        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        clean.indexOf("<caret>").takeIf { it >= 0 }?.let { myFixture.editor.caretModel.moveToOffset(it) }
        return file
    }

    private val menu = """
        shop-menu:
          title: '&eShop'
          width: 600
          content: {layout-mode: Top}
          elements:
            items: {layout-mode: TopScrolling, flex-weight: 1}
            close: {type: TextButton, special: true, text: Close}
          templates:
            item:
              layout-mode: Left
              children:
                name: {type: Label, text: '{NAME}'}
                buy: {type: TextButton, text: Buy}
    """.trimIndent()

    fun testBlockElementCompletionFollowsType() {
        open("resources/shop/menus.yml", "$menu\n        label:\n          type: Label\n          <caret>\n")
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings.orEmpty()
        assertTrue(items.toString(), "text" in items && "tooltip-text" in items)
        assertFalse(items.toString(), "layout-mode" in items)
    }

    fun testFlowElementCompletionFollowsType() {
        open("resources/shop/menus.yml", menu.replace("buy: {type: TextButton, text: Buy}", "buy: {type: TextButton, <caret>}"))
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings.orEmpty()
        assertTrue(items.toString(), "disabled" in items && "text" in items)
        assertFalse(items.toString(), "layout-mode" in items)
    }

    fun testMenuValuesAreChecked() {
        open(
            "resources/shop/menus.yml",
            menu.replace("width: 600", "width: wide").replace("items: {", "items: {background: blue, children: {close: {type: Label}}, ")
        )
        val messages = myFixture.doHighlighting(HighlightSeverity.WEAK_WARNING).mapNotNull { it.description }
        assertTrue(messages.toString(), messages.any { it == "'width' must be a whole number" })
        assertTrue(messages.toString(), messages.any { it.startsWith("'background' must be a color") })
        assertTrue(messages.toString(), messages.any { it.startsWith("The key 'close' is used twice") })
    }

    fun testJavaKeysResolveAndUnknownKeysAreReported() {
        myFixture.addFileToProject("resources/shop/menus.yml", menu)
        myFixture.enableInspections(HytaleMenuBindingInspection())
        myFixture.configureByText(
            "Use.java",
            """
            import com.golfing8.kcommon.menu.MenuBuilder;
            class Use extends demo.ShopModule {
                void use() {
                    MenuBuilder builder = getConfig("menus").getOrLoad("shop-menu", MenuBuilder.class).orElseThrow();
                    builder.bindTo("clo<caret>se", () -> {});
                    builder.bindTo("missing", () -> {});
                    builder.text("items", "x");
                    builder.addRow("items", "item").show("buy");
                }
            }
            """.trimIndent()
        )
        val target = myFixture.getReferenceAtCaretPositionWithAssertion().resolve()
        assertEquals("close", (target as YAMLKeyValue).keyText)
        val messages = myFixture.doHighlighting(HighlightSeverity.WARNING).mapNotNull { it.description }
        assertTrue(messages.toString(), "No element 'missing' in menu 'shop-menu'" in messages)
        assertTrue(messages.toString(), "'items' is a Group, which has no text" in messages)
        assertFalse(messages.toString(), messages.any { "'buy'" in it })
    }

    fun testLangFileKeysComeFromLangSourcesAndLangConf() {
        open("resources/shop/lang.yml", "not-enough-coins: '&cNot enough coins'\nbogus: x\n")
        val messages = myFixture.doHighlighting(HighlightSeverity.WEAK_WARNING).mapNotNull { it.description }
        assertTrue(messages.toString(), messages.any { it.startsWith("This lang file doesn't define bought, closed") })
        assertTrue(messages.toString(), "Unknown config key 'bogus' for module 'shop'" in messages)
    }
}
