package cc.me0wo.bingosplash.island.render

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.util.StringDecomposer

internal object IslandText {
    val regular =
        Style.EMPTY.withFont(FontDescription.Resource(Identifier.fromNamespaceAndPath("bingosplash", "island")))
            .withShadowColor(0)

    fun parse(value: String): Component {
        val normalized = value.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\r\n", "\n").replace('\r', '\n')
        val text = Component.empty()
        val segment = StringBuilder()
        var previous = regular
        StringDecomposer.iterateFormatted(Regex("&&|&([0-9a-fk-or])", RegexOption.IGNORE_CASE).replace(normalized) {
            if (it.value == "&&") "&" else "§${it.groupValues[1].lowercase()}"
        }, regular) { _, style, codePoint ->
            if (style != previous && segment.isNotEmpty()) {
                text.append(Component.literal(segment.toString()).setStyle(previous))
                segment.setLength(0)
            }
            previous = style
            segment.appendCodePoint(codePoint)
            true
        }
        if (segment.isNotEmpty()) text.append(Component.literal(segment.toString()).setStyle(previous))
        return text
    }
}
