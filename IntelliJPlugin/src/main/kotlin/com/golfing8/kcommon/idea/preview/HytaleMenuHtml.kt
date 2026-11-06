package com.golfing8.kcommon.idea.preview

/**
 * Turns a Hytale menu's config (plain maps, lists and strings, as `MenuBuilder(Map, String)` sees
 * it) into an HTML page that approximates how the client draws it.
 *
 * The page is built the way `MenuBuilder` builds markup, including the decorated frame, and each
 * element maps onto CSS the way the client lays it out (`Element.LayoutChildren` in the client):
 *  - `Full` (the default) places every child on its own against the whole parent: an Anchor side
 *    pins that edge, a size without a side centers, no size stretches. That's a one-cell CSS grid.
 *  - `Top`/`Middle`/`Bottom` (and the scrolling ones) stack children vertically, `Left`/`Center`/
 *    `Right` horizontally; `FlexWeight` shares out the leftover space. That's flexbox.
 *  - Patch backgrounds are nine-slices, which is `border-image` (slices doubled for `@2x` textures).
 * Fonts, text shrinking and scrollbars are approximate. Styles referenced as `@Name` are looked up
 * in the assets' `Common.ui`; without assets, textures fall back to flat colors.
 */
object HytaleMenuHtml {

    /**
     * @param showSpecial draws `special` elements, which in game only appear once Java binds or shows them.
     * @param rows container key -> template keys Java adds to it with `addRow`; each gets [sampleRows] copies.
     * @param sampleRows copies of each row; null uses the menu's `elements-per-page`, or 3.
     * @param samples placeholder -> sample value, filled into text (`{NAME}`).
     * @param highlight the element path (see [Element.path]) to outline, such as `elements/footer/children/close`.
     * @param zoom a fixed zoom, or null to shrink the frame to fit the window.
     * @param navigateScript JavaScript run with `path` set when an element is clicked.
     */
    data class Options(
        val showSpecial: Boolean = true,
        val rows: Map<String, List<String>> = emptyMap(),
        val sampleRows: Int? = null,
        val samples: Map<String, String> = emptyMap(),
        val highlight: String? = null,
        val zoom: Double? = null,
        val navigateScript: String? = null,
    )

    private val TITLE_STYLE = mapOf(
        "FontSize" to 15, "VerticalAlignment" to "Center", "RenderUppercase" to true, "TextColor" to "#b4c8c9",
        "FontName" to "Secondary", "RenderBold" to true, "LetterSpacing" to 0, "HorizontalAlignment" to "Center",
    )
    private val DEFAULT_BUTTON_LABEL = mapOf(
        "FontSize" to 17, "TextColor" to "#bfcdd5", "RenderBold" to true, "RenderUppercase" to true,
        "HorizontalAlignment" to "Center", "VerticalAlignment" to "Center",
    )

    private class Element(
        val key: String,
        val type: String,
        val special: Boolean,
        val text: String?,
        val props: Map<String, Any?>,
        val children: List<Element>,
        /** Where it is in the menu's YAML, `/`-separated (`templates/item/children/name`); null for the frame KCommon adds. */
        val path: String? = null,
    )

    fun render(menu: Map<String, Any?>, assets: MenuAssets?, options: Options = Options()): String =
        Renderer(menu, assets, options).page()

    private class Renderer(private val menu: Map<String, Any?>, private val assets: MenuAssets?, private val options: Options) {
        private val out = StringBuilder()
        private val templates: Map<String, Element> =
            asMap(menu["templates"]).mapNotNull { (key, raw) -> parse(key, raw, "templates/$key")?.let { key to it } }.toMap()
        private val sampleRows = options.sampleRows ?: int(menu["elements-per-page"])?.takeIf { it > 0 } ?: 3
        private val placed = HashSet<String>()

        fun page(): String {
            val width = int(menu["width"]) ?: 600
            val height = int(menu["height"]) ?: 400
            val overlay = bool(menu["overlay"]) == true
            val decorated = menu["frame"]?.toString()?.lowercase() != "none"
            val title = menu["title"]?.let(::joinLines)

            val contentProps = LinkedHashMap<String, Any?>()
            if (decorated) {
                contentProps["LayoutMode"] = "Top"
                contentProps["Anchor"] = mapOf("Top" to 38)
                contentProps["Padding"] = mapOf("Full" to 17)
                contentProps["Background"] = mapOf("TexturePath" to "Common/ContainerPatch.png", "Border" to 23)
            }
            merge(contentProps, props(asMap(menu["content"])))
            val content = Element("content", "Group", false, null, contentProps, children(menu["elements"], "elements"), "content")

            val root = if (!decorated) content else Element(
                "frame", "Group", false, null, mapOf("Anchor" to mapOf("Width" to width, "Height" to height)),
                listOf(
                    Element(
                        "header", "Group", false, null,
                        mapOf(
                            "Anchor" to mapOf("Height" to 38, "Top" to 0),
                            "Background" to mapOf("TexturePath" to "Common/ContainerHeader.png", "HorizontalBorder" to 50, "VerticalBorder" to 0),
                            "Padding" to mapOf("Top" to 7),
                        ),
                        listOfNotNull(
                            decoration("Common/ContainerDecorationTop.png", "Top" to -12),
                            title?.let { Element("title", "Label", false, it, mapOf("Style" to TITLE_STYLE, "Padding" to mapOf("Horizontal" to 19)), emptyList()) },
                        )
                    ),
                    content,
                    decoration("Common/ContainerDecorationBottom.png", "Bottom" to -6),
                )
            )

            out.append("<!doctype html><html><head><meta charset=\"utf-8\"><style>").append(CSS).append("</style></head><body>")
            out.append("<div class=\"desktop").append(if (overlay) " overlay" else "").append("\">")
            element(root, "Full")
            out.append("</div>")

            val unplaced = templates.filterKeys { it !in placed }
            if (unplaced.isNotEmpty()) {
                out.append("<div class=\"templates\"><div class=\"caption\">Templates (not added to a list by any addRow call found)</div>")
                for ((key, template) in unplaced) {
                    out.append("<div class=\"caption\">").append(escape(key)).append("</div>")
                    out.append("<div class=\"template\" style=\"width:").append(width - 34).append("px\">")
                    element(template, "Top")
                    out.append("</div>")
                }
                out.append("</div>")
            }
            out.append("<script>")
            if (options.zoom != null) {
                out.append("document.querySelector('.desktop>.el').style.zoom=").append(options.zoom).append(';')
            } else if (decorated) {
                // Shrink a frame wider or taller than the tool window to fit, rather than clipping it.
                out.append("(function(){function fit(){var s=Math.min(1,(innerWidth-48)/")
                    .append(width).append(",(innerHeight-48)/").append(height + 24)
                    .append(");document.querySelector('.desktop>.el').style.zoom=Math.max(s,0.25);}addEventListener('resize',fit);fit();})();")
            }
            out.append(SCRIPT)
            options.navigateScript?.let { out.append("window.kcNavigate=function(path){").append(it).append("};") }
            options.highlight?.let { out.append("kcHighlight(").append(jsString(it)).append(");") }
            out.append("</script>")
            if (assets == null) {
                out.append("<div class=\"note\">No Hytale assets found, so textures and .ui styles are approximated. ")
                    .append("Point Settings | Tools | KCommon at your Hytale Assets.zip.</div>")
            }
            return out.append("</body></html>").toString()
        }

        private fun decoration(texture: String, offset: Pair<String, Int>) = Element(
            "decoration", "Group", false, null,
            mapOf("Anchor" to mapOf("Width" to 236, "Height" to 11, offset), "Background" to mapOf("TexturePath" to texture)),
            emptyList()
        )

        private fun element(element: Element, parentLayout: String) {
            if (element.special && !options.showSpecial) return
            if (bool(element.props["Visible"]) == false) return

            val style = StringBuilder(placement(asMap(element.props["Anchor"]), element.props["FlexWeight"], parentLayout))
            padding(asMap(element.props["Padding"]))?.let(style::append)

            val known = element.type in ELEMENT_TYPES
            val layout = when (element.type) {
                "StyledText" -> element.props["LayoutMode"]?.toString() ?: "Left"
                "Label", "Title", "TextButton" -> null
                else -> element.props["LayoutMode"]?.toString() ?: "Full"
            }
            style.append(layout?.let(::layoutCss) ?: "display:flex;")

            out.append("<div class=\"el t-").append(escape(element.type)).append(if (element.special) " special" else "").append(if (!known) " unknown" else "")
                .append("\" title=\"").append(escape("${element.key} (${element.type})")).append("\" style=\"").append(escape(style.toString())).append('"')
            element.path?.let { out.append(" data-path=\"").append(escape(it)).append('"') }
            out.append('>')

            var background = element.props["Background"]
            var labelStyle: Map<String, Any?>? = null
            if (element.type == "TextButton" || element.type == "Button") {
                val state = asMap(asMap(resolveRef(element.props["Style"]))[if (bool(element.props["Disabled"]) == true) "Disabled" else "Default"])
                background = background ?: state["Background"] ?: if (state.isEmpty()) "#2c3a52" else null
                labelStyle = asMap(state["LabelStyle"]).ifEmpty { DEFAULT_BUTTON_LABEL }
            }
            background(background)

            when (element.type) {
                "Label", "Title", "StyledText", "TextButton" -> {
                    val own = asMap(resolveRef(element.props["Style"]).takeIf { element.type != "TextButton" })
                    val base = when (element.type) {
                        "Title" -> TITLE_STYLE
                        "TextButton" -> labelStyle.orEmpty()
                        "StyledText" -> mapOf("FontSize" to (int(element.props["FontSize"]) ?: 16), "RenderBold" to (bool(element.props["Bold"]) ?: false))
                        else -> emptyMap()
                    }
                    label(element.text, LinkedHashMap(base).apply { putAll(own) })
                }
                "ItemIcon", "ItemSlot" -> item(element)
                "AssetImage" -> element.props["AssetPath"]?.toString()?.let { assets?.image(it) }?.let {
                    out.append("<div class=\"fill image\" style=\"background-image:url('").append(it).append("')\"></div>")
                }
                "ProgressBar" -> progress(element)
                "TextField" -> textField(element)
                else -> if (!known) out.append("<div class=\"error\">Unknown element type '").append(escape(element.type)).append("'</div>")
            }

            val childLayout = layout ?: "Full"
            for (child in element.children) element(child, childLayout)
            for (templateKey in options.rows[element.key].orEmpty()) {
                val template = templates[templateKey] ?: continue
                placed += templateKey
                repeat(sampleRows) { element(template, childLayout) }
            }
            out.append("</div>")
        }

        private fun label(text: String?, style: Map<String, Any?>) {
            if (text == null) return
            val css = StringBuilder()
            css.append("justify-content:").append(flexAlign(style["HorizontalAlignment"])).append(';')
            css.append("align-items:").append(flexAlign(style["VerticalAlignment"])).append(';')
            css.append("text-align:").append(textAlign(style["HorizontalAlignment"])).append(';')
            css.append("font-size:").append(int(style["FontSize"]) ?: 16).append("px;")
            if (bool(style["RenderBold"]) == true) css.append("font-weight:700;")
            if (bool(style["RenderItalics"]) == true) css.append("font-style:italic;")
            if (bool(style["RenderUnderlined"]) == true) css.append("text-decoration:underline;")
            if (bool(style["RenderUppercase"]) == true) css.append("text-transform:uppercase;")
            if (bool(style["Wrap"]) == true) css.append("white-space:pre-wrap;") else css.append("white-space:pre;")
            num(style["LetterSpacing"])?.let { css.append("letter-spacing:").append(it).append("px;") }
            if (style["FontName"]?.toString() == "Secondary") css.append("font-family:var(--secondary);")
            color(style["OutlineColor"])?.let { css.append("text-shadow:0 0 1px $it,0 0 1px $it;") }
            val base = color(style["TextColor"]) ?: "#ffffff"
            out.append("<div class=\"label\" style=\"").append(css).append("\"><span>")
            val filled = options.samples.entries.fold(text) { current, (key, value) -> current.replace("{$key}", value) }
            out.append(TextFormat.toHtml(filled, base))
            out.append("</span></div>")
        }

        private fun item(element: Element) {
            val id = element.props["ItemId"]?.toString()
            if (element.type == "ItemSlot") out.append("<div class=\"fill slot\"></div>")
            val icon = id?.let { assets?.itemIcon(it) }
            if (icon != null) {
                out.append("<div class=\"fill image\" style=\"background-image:url('").append(icon).append("')\"></div>")
            } else {
                out.append("<div class=\"fill missing-item\">").append(escape(id ?: "item")).append("</div>")
            }
            val quantity = int(element.props["Quantity"])
            if (element.type == "ItemSlot" && quantity != null && quantity > 1 && bool(element.props["ShowQuantity"]) != false) {
                out.append("<div class=\"quantity\">").append(quantity).append("</div>")
            }
        }

        private fun progress(element: Element) {
            val value = ((num(element.props["Value"]) ?: 0.5) * 100).coerceIn(0.0, 100.0)
            val vertical = element.props["Alignment"]?.toString() == "Vertical"
            val fromEnd = element.props["Direction"]?.toString() == "End"
            val size = if (vertical) "height:$value%;width:100%;" else "width:$value%;height:100%;"
            val anchor = if (vertical) (if (fromEnd) "top:0;" else "bottom:0;") else (if (fromEnd) "right:0;" else "left:0;")
            out.append("<div class=\"bar\" style=\"").append(size).append(anchor).append("\">")
            background(element.props["Bar"] ?: element.props["BarTexturePath"] ?: "#4f8fd8")
            out.append("</div>")
        }

        private fun textField(element: Element) {
            if (element.props["Background"] == null) background(mapOf("TexturePath" to "Common/InputBox.png", "Border" to 16))
            val placeholder = element.props["PlaceholderText"]?.toString() ?: return
            out.append("<div class=\"label\" style=\"align-items:center;padding:0 10px;color:#6e7da1;font-size:16px\">")
                .append(escape(placeholder)).append("</div>")
        }

        /** A patch background: a color, a texture path, or a PatchStyle tuple. */
        private fun background(value: Any?) {
            val patch = when (val resolved = resolveRef(value)) {
                null -> return
                is Map<*, *> -> asMap(resolved)
                else -> resolved.toString().let { if (it.endsWith(".png")) mapOf("TexturePath" to it) else mapOf("Color" to it) }
            }
            val css = StringBuilder()
            color(patch["Color"])?.let { css.append("background-color:").append(it).append(';') }
            val path = patch["TexturePath"]?.toString()
            if (path != null) {
                val texture = assets?.texture(path)
                if (texture == null) {
                    css.append("background-color:rgba(44,58,82,.85);box-shadow:inset 0 0 0 1px rgba(150,169,190,.35);")
                } else {
                    val border = int(patch["Border"]) ?: 0
                    val vertical = int(patch["VerticalBorder"]) ?: border
                    val horizontal = int(patch["HorizontalBorder"]) ?: border
                    if (vertical == 0 && horizontal == 0) {
                        css.append("background:url('").append(texture.uri).append("') 0 0/100% 100% no-repeat;")
                    } else {
                        css.append("border-style:solid;border-width:${vertical}px ${horizontal}px;")
                            .append("border-image:url('").append(texture.uri).append("') ")
                            .append(vertical * texture.scale).append(' ').append(horizontal * texture.scale).append(" fill stretch;")
                    }
                }
            }
            if (css.isNotEmpty()) out.append("<div class=\"fill bg\" style=\"").append(css).append("\"></div>")
        }

        private fun resolveRef(value: Any?): Any? {
            val ref = when (value) {
                is String -> value.takeIf { REF.matches(it.trim()) }
                is Map<*, *> -> value.entries.firstOrNull { it.key.toString().equals("ref", ignoreCase = true) }?.value?.toString()
                else -> null
            } ?: return value
            return assets?.reference(ref.trim())
        }

        private fun parse(key: String, raw: Any?, path: String, defaultType: String = "Group"): Element? {
            val section = asMap(raw).ifEmpty { return null }
            val type = section["type"]?.toString() ?: defaultType
            val rest = section.filterKeys { it != "type" && it != "special" && it != "text" && it != "children" }
            return Element(key, type, bool(section["special"]) == true, section["text"]?.let(::joinLines), props(rest), children(section["children"], "$path/children"), path)
        }

        private fun children(raw: Any?, path: String): List<Element> = asMap(raw).mapNotNull { (key, value) -> parse(key, value, "$path/$key") }
    }

    // --- layout ---

    private val ELEMENT_TYPES = setOf("Group", "Label", "Title", "StyledText", "TextButton", "Button", "ItemIcon", "ItemSlot", "AssetImage", "ProgressBar", "TextField")
    private val REF = Regex("(?:[A-Za-z0-9_/]+\\.ui)?@[A-Za-z][A-Za-z0-9]*")
    private val COLUMN = setOf("Top", "Middle", "Bottom", "TopScrolling", "BottomScrolling", "MiddleCenter")
    private val ROW = setOf("Left", "Center", "Right", "LeftScrolling", "RightScrolling", "CenterMiddle", "LeftWrap", "LeftCenterWrap")

    private fun layoutCss(mode: String): String = when (mode) {
        "Top" -> "display:flex;flex-direction:column;"
        "TopScrolling" -> "display:flex;flex-direction:column;overflow-y:auto;"
        "Middle" -> "display:flex;flex-direction:column;justify-content:center;"
        "Bottom" -> "display:flex;flex-direction:column;justify-content:flex-end;"
        "BottomScrolling" -> "display:flex;flex-direction:column;justify-content:flex-end;overflow-y:auto;"
        "MiddleCenter" -> "display:flex;flex-direction:column;justify-content:center;align-items:center;"
        "Left" -> "display:flex;flex-direction:row;"
        "LeftScrolling" -> "display:flex;flex-direction:row;overflow-x:auto;"
        "Center" -> "display:flex;flex-direction:row;justify-content:center;"
        "Right" -> "display:flex;flex-direction:row;justify-content:flex-end;"
        "RightScrolling" -> "display:flex;flex-direction:row;justify-content:flex-end;overflow-x:auto;"
        "CenterMiddle" -> "display:flex;flex-direction:row;justify-content:center;align-items:center;"
        "LeftWrap" -> "display:flex;flex-direction:row;flex-wrap:wrap;align-content:flex-start;"
        "LeftCenterWrap" -> "display:flex;flex-direction:row;flex-wrap:wrap;justify-content:center;align-content:flex-start;"
        else -> "display:grid;grid-template:minmax(0,1fr)/minmax(0,1fr);"
    }

    /** CSS placing an element with [anchor] inside a parent laid out with [parentLayout]. */
    private fun placement(anchor: Map<String, Any?>, flexWeight: Any?, parentLayout: String): String {
        val left = int(anchor["Left"]) ?: int(anchor["Horizontal"]) ?: int(anchor["Full"])
        val right = int(anchor["Right"]) ?: int(anchor["Horizontal"]) ?: int(anchor["Full"])
        val top = int(anchor["Top"]) ?: int(anchor["Vertical"]) ?: int(anchor["Full"])
        val bottom = int(anchor["Bottom"]) ?: int(anchor["Vertical"]) ?: int(anchor["Full"])
        val width = int(anchor["Width"])
        val height = int(anchor["Height"])
        val css = StringBuilder()
        int(anchor["MinWidth"])?.let { css.append("min-width:${it}px;") }
        int(anchor["MaxWidth"])?.let { css.append("max-width:${it}px;") }
        val weight = num(flexWeight)?.takeIf { it > 0 }
        val centered = parentLayout == "MiddleCenter" || parentLayout == "CenterMiddle"

        fun cross(size: Int?, start: Int?, end: Int?, sizeProp: String, startProp: String, endProp: String, selfProp: String) {
            if (size != null) {
                css.append("$sizeProp:${size}px;")
                when {
                    start != null -> css.append("$selfProp:start;margin-$startProp:${start}px;")
                    end != null -> css.append("$selfProp:end;margin-$endProp:${end}px;")
                    else -> css.append("$selfProp:safe center;")
                }
            } else if (centered && start == null && end == null) {
                css.append("$selfProp:safe center;")
            } else {
                css.append("$selfProp:stretch;margin-$startProp:${start ?: 0}px;margin-$endProp:${end ?: 0}px;")
            }
        }

        fun main(size: Int?, start: Int?, end: Int?, sizeProp: String, startProp: String, endProp: String) {
            when {
                size != null -> css.append("$sizeProp:${size}px;flex:0 0 auto;")
                weight != null -> css.append("flex:$weight 1 0;min-$sizeProp:0;")
                else -> css.append("flex:0 0 auto;")
            }
            start?.let { css.append("margin-$startProp:${it}px;") }
            end?.let { css.append("margin-$endProp:${it}px;") }
        }

        when (parentLayout) {
            in COLUMN -> {
                main(height, top, bottom, "height", "top", "bottom")
                cross(width, left, right, "width", "left", "right", "align-self")
            }
            in ROW -> {
                main(width, left, right, "width", "left", "right")
                cross(height, top, bottom, "height", "top", "bottom", "align-self")
            }
            else -> {
                css.append("grid-area:1/1;")
                cross(width, left, right, "width", "left", "right", "justify-self")
                cross(height, top, bottom, "height", "top", "bottom", "align-self")
            }
        }
        return css.toString()
    }

    private fun padding(padding: Map<String, Any?>): String? {
        if (padding.isEmpty()) return null
        fun side(name: String, axis: String) = int(padding[name]) ?: int(padding[axis]) ?: int(padding["Full"]) ?: 0
        return "padding:${side("Top", "Vertical")}px ${side("Right", "Horizontal")}px ${side("Bottom", "Vertical")}px ${side("Left", "Horizontal")}px;"
    }

    private fun flexAlign(value: Any?) = when (value?.toString()) {
        "Center" -> "center"
        "End" -> "flex-end"
        else -> "flex-start"
    }

    private fun textAlign(value: Any?) = when (value?.toString()) {
        "Center" -> "center"
        "End" -> "right"
        else -> "left"
    }

    // --- values ---

    /** `#rrggbb`, `#rrggbbaa` or `#rrggbb(0.5)` as a CSS color. */
    internal fun color(value: Any?): String? {
        val text = value?.toString()?.trim() ?: return null
        val match = Regex("#([0-9a-fA-F]{6})([0-9a-fA-F]{2})?(?:\\(\\s*([0-9.]+)\\s*\\))?").matchEntire(text) ?: return null
        val rgb = match.groupValues[1]
        val alpha = match.groupValues[3].toDoubleOrNull() ?: match.groupValues[2].takeIf { it.isNotEmpty() }?.let { it.toInt(16) / 255.0 }
        if (alpha == null) return "#$rgb"
        return "rgba(${rgb.substring(0, 2).toInt(16)},${rgb.substring(2, 4).toInt(16)},${rgb.substring(4, 6).toInt(16)},$alpha)"
    }

    private fun props(raw: Map<String, Any?>): Map<String, Any?> {
        val props = LinkedHashMap<String, Any?>()
        for ((key, value) in raw) props[pascal(key)] = pascalKeys(value)
        return props
    }

    /** Tuple keys are case-insensitive kebab too (`anchor: {height: 44}`), except a `{ref: ...}` reference. */
    private fun pascalKeys(value: Any?): Any? {
        if (value !is Map<*, *>) return value
        return value.entries.associate { pascal(it.key.toString()) to pascalKeys(it.value) }
    }

    /** Copies [from] into [into], merging tuples key by key, as `MenuBuilder.merge` does. */
    private fun merge(into: MutableMap<String, Any?>, from: Map<String, Any?>) {
        for ((key, value) in from) {
            val old = into[key]
            into[key] = if (old is Map<*, *> && value is Map<*, *> && !value.containsKey("Ref")) asMap(old) + asMap(value) else value
        }
    }

    /** Mirrors `MenuSchema.pascal`. */
    internal fun pascal(key: String): String {
        val out = StringBuilder(key.length)
        var upper = true
        for (c in key) {
            if (c == '-' || c == '_' || c == ' ') {
                upper = true
            } else {
                out.append(if (upper) c.uppercaseChar() else c)
                upper = false
            }
        }
        return out.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun asMap(value: Any?): Map<String, Any?> = (value as? Map<String, Any?>).orEmpty()

    private fun joinLines(value: Any): String = if (value is List<*>) value.joinToString("\n") else value.toString()

    private fun num(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        else -> value?.toString()?.trim()?.toDoubleOrNull()
    }

    private fun int(value: Any?): Int? = num(value)?.toInt()

    private fun bool(value: Any?): Boolean? = when (value?.toString()?.trim()) {
        "true" -> true
        "false" -> false
        else -> null
    }

    internal fun escape(text: String): String = buildString(text.length) {
        for (c in text) when (c) {
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '&' -> append("&amp;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(c)
        }
    }

    /** `kcHighlight(path)` outlines the element at path (or its nearest ancestor shown); a click calls `kcNavigate(path)` when the IDE set one. */
    internal const val SCRIPT = "window.kcHighlight=function(p){document.querySelectorAll('.kc-hl').forEach(function(e){e.classList.remove('kc-hl')});" +
        "while(p){var m=document.querySelectorAll('[data-path=\"'+p+'\"]');if(m.length){m.forEach(function(e){e.classList.add('kc-hl')});" +
        "m[0].scrollIntoView({block:'nearest',inline:'nearest'});return;}var i=p.lastIndexOf('/');p=i<0?'':p.substring(0,i);}};" +
        "document.addEventListener('click',function(e){var t=e.target.closest('[data-path]');if(t&&window.kcNavigate){e.preventDefault();window.kcNavigate(t.getAttribute('data-path'));}});"

    /** A JavaScript string literal. */
    internal fun jsString(text: String): String = buildString {
        append('\'')
        for (c in text) when (c) {
            '\'', '\\' -> append('\\').append(c)
            '\n' -> append("\\n")
            '<' -> append("\\u003c")
            else -> append(c)
        }
        append('\'')
    }

    private const val CSS = """
:root{--secondary:'Lexend','Nunito Sans','Segoe UI',sans-serif;}
html,body{margin:0;background:#141a24;color:#fff;font-family:'Nunito Sans','Segoe UI',Roboto,sans-serif;}
.desktop{display:grid;grid-template:minmax(0,1fr)/minmax(0,1fr);min-height:100vh;box-sizing:border-box;padding:24px;
  background:radial-gradient(ellipse at center,#2b3a4f 0%,#141a24 75%);}
.desktop.overlay{background:linear-gradient(rgba(0,0,0,.45),rgba(0,0,0,.45)),radial-gradient(ellipse at center,#2b3a4f 0%,#141a24 75%);}
.el{position:relative;box-sizing:border-box;min-width:0;}
.el>*:not(.fill){position:relative;}
.fill{position:absolute;inset:0;box-sizing:border-box;pointer-events:none;}
.label{display:flex;flex:1 1 auto;overflow:hidden;line-height:1.2;}
.image{background-size:contain;background-position:center;background-repeat:no-repeat;}
.slot{background:rgba(0,0,0,.35);box-shadow:inset 0 0 0 1px rgba(150,169,190,.3);}
.missing-item{display:flex;align-items:center;justify-content:center;font-size:9px;color:#96a9be;text-align:center;overflow:hidden;
  outline:1px dashed rgba(150,169,190,.5);outline-offset:-3px;word-break:break-all;}
.quantity{position:absolute!important;right:3px;bottom:1px;font-size:13px;font-weight:700;text-shadow:0 1px 2px #000;}
.bar{position:absolute;}
.t-ItemIcon,.t-ItemSlot{min-width:32px;min-height:32px;}
.special::before,.kc-hl::after{content:'';position:absolute;inset:0;pointer-events:none;z-index:5;box-sizing:border-box;}
.special::before{border:1px dashed rgba(255,190,80,.85);}
.kc-hl::after{border:2px solid #4da3ff;box-shadow:0 0 6px rgba(77,163,255,.6);}
[data-path]{cursor:pointer;}
.unknown{box-shadow:inset 0 0 0 1px #e05555;}
.error{color:#ff7070;font-size:12px;padding:4px;}
.templates{padding:16px 24px;border-top:1px solid #2b3542;}
.template{display:flex;flex-direction:column;margin:4px 0 12px;background:rgba(0,0,0,.2);}
.caption{color:#96a9be;font-size:12px;margin-top:6px;}
.note{position:fixed;left:8px;bottom:8px;right:8px;font-size:12px;color:#c9d1d9;background:rgba(0,0,0,.6);padding:6px 8px;border-radius:4px;}
"""
}
