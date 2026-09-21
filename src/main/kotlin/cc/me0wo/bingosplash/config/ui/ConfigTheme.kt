package cc.me0wo.bingosplash.config.ui

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.component.SliderComponent
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.util.NinePatchTexture
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier

object ConfigTheme {
    private val panel = Color.ofArgb(0xB0141A32.toInt())
    private val border = Color.ofArgb(0xD05D6B98.toInt())
    private val field = Color.ofArgb(0xC20B1022.toInt())
    private val fieldBorder = Color.ofArgb(0x80515D88.toInt())
    private val accent = Color.ofArgb(0xFF78E6D2.toInt())
    private val text = Color.ofArgb(0xFFE8E9FF.toInt())
    private val button = Color.ofArgb(0xD92E3758.toInt())
    private val hovered = Color.ofArgb(0xF05B6F9E.toInt())
    private val disabled = Color.ofArgb(0x8830394E.toInt())
    private val panelTexture = texture("rounded_panel", 16, 32, 64)
    private val controlTexture = texture("rounded_control", 8, 16, 32)
    private val panelSurface = surface(panelTexture, panel, border)
    private val controlSurface = surface(controlTexture, field, fieldBorder)
    private val buttonRenderer = ButtonComponent.Renderer { graphics, button, _ ->
        controlTexture.draw(
            graphics, button.x, button.y, button.width, button.height, when {
                !button.active -> disabled
                button.isHovered -> hovered
                else -> this.button
            }
        )
    }

    fun apply(root: FlowLayout) {
        root.surface(Surface.vanillaPanorama(false).and(Surface.blur(10f, 20f)).and(Surface.flat(0x8F0D1226.toInt())))
        root.childById(FlowLayout::class.java, "main-panel").apply {
            margins(Insets.of(12)); padding(Insets.of(8)); surface(panelSurface)
        }
        root.childById(FlowLayout::class.java, "option-panel").padding(Insets.of(6))
        root.forEachDescendant { component ->
            when (component) {
                is ButtonComponent -> component.renderer(buttonRenderer)
                is TextBoxComponent -> {
                    component.setBordered(false); component.setTextColor(accent.rgb()); component.parent()
                        ?.surface(controlSurface)
                }

                is SliderComponent -> {
                    component.setAlpha(1f); component.margins(Insets.of(2)); component.parent()?.surface(controlSurface)
                }

                is LabelComponent -> when (component.id()) {
                    "title" -> component.color(text)
                    "header" -> component.text(
                        component.text().copy().withStyle(Style.EMPTY.withColor(accent.rgb()).withBold(true))
                    )
                }
            }
        }
    }

    private fun texture(name: String, corner: Int, center: Int, size: Int) = NinePatchTexture(
        Identifier.fromNamespaceAndPath("bingosplash", "textures/gui/$name.png"),
        0, 0, Size.square(corner), Size.square(center), Size.square(size), false
    )

    private fun surface(texture: NinePatchTexture, fill: Color, outline: Color) = Surface { graphics, component ->
        texture.draw(graphics, component.x(), component.y(), component.width(), component.height(), outline)
        texture.draw(
            graphics,
            component.x() + 1,
            component.y() + 1,
            component.width() - 2,
            component.height() - 2,
            fill
        )
    }
}
