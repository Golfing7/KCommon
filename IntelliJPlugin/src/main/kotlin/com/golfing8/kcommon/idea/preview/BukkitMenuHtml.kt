package com.golfing8.kcommon.idea.preview

/**
 * Draws a Bukkit `MenuBuilder` config as the inventory a player sees, mirroring
 * `MenuBuilder(ConfigurationSection)`: the inventory shape, the filler item and its shape, then
 * `other-slots` and (once bound, so optionally) `special-slots`, which win over everything else.
 * Hovering a slot shows the item's name and lore. Textures come from [MinecraftAssets].
 */
object BukkitMenuHtml {

    private data class Shape(val width: Int, val defaultSize: Int)

    private val SHAPES = mapOf(
        "CHEST" to Shape(9, 27), "DISPENSER" to Shape(3, 9), "HOPPER" to Shape(5, 5), "ANVIL" to Shape(3, 3),
    )

    private class Item(val material: String, val name: String?, val lore: List<String>, val amount: Int, val glowing: Boolean, val path: String, val special: Boolean)

    fun render(menu: Map<String, Any?>, textures: MinecraftAssets.ItemTextures?, options: HytaleMenuHtml.Options = HytaleMenuHtml.Options()): String {
        val typeName = menu["type"]?.toString()?.uppercase()
        val shape = SHAPES[typeName] ?: SHAPES.getValue("CHEST")
        val size = int(menu["size"]) ?: if (typeName != null) shape.defaultSize else 27
        val width = shape.width
        val slots = arrayOfNulls<Item>(size.coerceIn(1, 54))

        fun slotOf(value: Any?): Int? = when (value) {
            is Map<*, *> -> {
                val x = int(value["x"]) ?: return null
                val y = int(value["y"]) ?: return null
                (y - 1) * width + (x - 1)
            }
            else -> int(value)
        }

        val useFiller = bool(menu["use-filler-item"]) == true || (menu.containsKey("filler-item") && !menu.containsKey("use-filler-item"))
        if (useFiller) {
            val filler = item(asMap(menu["filler-item"]).ifEmpty { mapOf("type" to "GRAY_STAINED_GLASS_PANE", "name" to "&7") }, "filler-item", false)
            val fillerShape = asMap(menu["filler-shape"])
            val targets: Collection<Int> = when (fillerShape["type"]?.toString()?.uppercase()) {
                "RECTANGLE", "OUTLINE" -> {
                    val low = slotOf(fillerShape["low-slot"]) ?: 0
                    val high = slotOf(fillerShape["high-slot"]) ?: (slots.size - 1)
                    val (lx, ly) = low % width to low / width
                    val (hx, hy) = high % width to high / width
                    val outline = fillerShape["type"].toString().equals("OUTLINE", ignoreCase = true)
                    (ly..hy).flatMap { y -> (lx..hx).filter { x -> !outline || x == lx || x == hx || y == ly || y == hy }.map { x -> y * width + x } }
                }
                "POINTS" -> (fillerShape["points"] as? List<*>).orEmpty().mapNotNull(::slotOf)
                else -> slots.indices.toList()
            }
            for (slot in targets) if (slot in slots.indices) slots[slot] = filler
        }

        fun place(section: String, special: Boolean) {
            if (special && !options.showSpecial) return
            for ((key, raw) in asMap(menu[section])) {
                val config = asMap(raw)
                val item = item(asMap(config["item"]).ifEmpty { config }, "$section/$key", special)
                val targets = (config["slots"] as? List<*>)?.mapNotNull(::slotOf) ?: listOfNotNull(slotOf(config["slot"]))
                for (slot in targets) if (slot in slots.indices) slots[slot] = item
            }
        }
        place("other-slots", false)
        place("special-slots", true)

        val out = StringBuilder("<!doctype html><html><head><meta charset=\"utf-8\"><style>").append(CSS).append("</style></head><body><div class=\"desktop\">")
        out.append("<div class=\"inventory\" style=\"--cols:").append(width).append("\">")
        out.append("<div class=\"title\">").append(TextFormat.toHtml(menu["title"]?.toString() ?: "", "#404040")).append("</div><div class=\"grid\">")
        for (item in slots) {
            if (item == null) {
                out.append("<div class=\"slot\"></div>")
                continue
            }
            out.append("<div class=\"slot").append(if (item.special) " special" else "").append("\" data-path=\"").append(HytaleMenuHtml.escape(item.path)).append("\">")
            val texture = textures?.item(item.material)
            if (texture != null) {
                out.append("<div class=\"icon").append(if (item.glowing) " glow" else "").append("\" style=\"background-image:url('").append(texture).append("')\"></div>")
            } else {
                out.append("<div class=\"missing\">").append(HytaleMenuHtml.escape(item.material.lowercase().replace('_', ' '))).append("</div>")
            }
            if (item.amount > 1) out.append("<div class=\"amount\">").append(item.amount).append("</div>")
            out.append("<div class=\"tooltip\"><div>").append(TextFormat.toHtml(item.name ?: item.material.lowercase().replace('_', ' '), "#ffffff")).append("</div>")
            for (line in item.lore) out.append("<div>").append(TextFormat.toHtml(line, "#aa00aa")).append("</div>")
            out.append("</div></div>")
        }
        out.append("</div></div></div><script>")
        options.zoom?.let { out.append("document.querySelector('.inventory').style.zoom=").append(it).append(';') }
        out.append(HytaleMenuHtml.SCRIPT)
        options.navigateScript?.let { out.append("window.kcNavigate=function(path){").append(it).append("};") }
        options.highlight?.let { out.append("kcHighlight(").append(HytaleMenuHtml.jsString(it)).append(");") }
        out.append("</script>")
        if (textures == null) {
            out.append("<div class=\"note\">No Minecraft client jar found, so items are shown by name. Point Settings | Tools | KCommon at one.</div>")
        }
        return out.append("</body></html>").toString()
    }

    private fun item(config: Map<String, Any?>, path: String, special: Boolean): Item {
        val lore = when (val raw = config["lore"]) {
            is List<*> -> raw.map { it.toString() }
            null -> emptyList()
            else -> listOf(raw.toString())
        }
        return Item(
            config["type"]?.toString() ?: "STONE", config["name"]?.toString(), lore,
            int(config["amount"])?.coerceAtLeast(1) ?: 1, bool(config["glowing"]) == true || asMap(config["enchantments"]).isNotEmpty(), path, special
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun asMap(value: Any?): Map<String, Any?> = (value as? Map<String, Any?>).orEmpty()

    private fun int(value: Any?): Int? = value?.toString()?.trim()?.toDoubleOrNull()?.toInt()

    private fun bool(value: Any?): Boolean? = when (value?.toString()?.trim()) {
        "true" -> true
        "false" -> false
        else -> null
    }

    private const val CSS = """
html,body{margin:0;background:#1d1d1d;font-family:'Minecraft','Monocraft','Segoe UI',monospace;}
.desktop{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;box-sizing:border-box;
  background:radial-gradient(ellipse at center,#3a3a3a 0%,#1d1d1d 75%);}
.inventory{background:#c6c6c6;border:2px solid;border-color:#fff #555 #555 #fff;border-radius:3px;padding:8px 10px 10px;box-shadow:0 0 0 2px #000;}
.title{color:#404040;font-size:15px;margin:0 0 6px 2px;white-space:pre;}
.grid{display:grid;grid-template-columns:repeat(var(--cols),36px);}
.slot{position:relative;width:36px;height:36px;box-sizing:border-box;background:#8b8b8b;border:2px solid;border-color:#373737 #fff #fff #373737;}
.slot:hover{background:#c5c5c5;}
.icon{position:absolute;inset:2px;background-size:100% auto;background-position:top;background-repeat:no-repeat;image-rendering:pixelated;}
.glow{filter:drop-shadow(0 0 2px #c77dff) saturate(1.3);}
.missing{position:absolute;inset:1px;font-size:7px;line-height:8px;color:#222;overflow:hidden;word-break:break-all;}
.amount{position:absolute;right:1px;bottom:-1px;color:#fff;font-size:13px;text-shadow:2px 2px 0 #3f3f3f;}
.tooltip{display:none;position:absolute;left:30px;top:-6px;z-index:9;min-width:140px;padding:5px 7px;white-space:pre;
  background:rgba(16,0,16,.94);border:2px solid #2d0a63;border-radius:3px;font-size:13px;line-height:17px;pointer-events:none;}
.slot:hover .tooltip{display:block;}
.special::before,.kc-hl::after{content:'';position:absolute;inset:0;pointer-events:none;z-index:5;}
.special::before{border:1px dashed rgba(255,190,80,.95);}
.kc-hl::after{border:2px solid #4da3ff;}
[data-path]{cursor:pointer;}
.note{position:fixed;left:8px;bottom:8px;right:8px;font-size:12px;color:#ddd;background:rgba(0,0,0,.6);padding:6px 8px;border-radius:4px;}
"""
}
