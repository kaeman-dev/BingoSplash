package cc.me0wo.bingosplash.island.render

import cc.me0wo.bingosplash.island.IslandPhase
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.hud.Hud
import io.wispforest.owo.ui.util.NinePatchTexture
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

internal object IslandHud {
    private val id = Identifier.fromNamespaceAndPath("bingosplash", "island")
    private val background = NinePatchTexture(
        Identifier.fromNamespaceAndPath("bingosplash", "textures/gui/island.png"),
        0, 0, Size.square(44), Size.of(88, 0), Size.of(176, 88), false
    )

    private data class View(
        val panel: FlowLayout, val label: LabelComponent, val icon: IslandIcon?,
        val width: Int, var fading: Boolean = false
    )

    private fun build(
        textValue: String,
        iconValue: String,
        actions: List<String>,
        rockDegrees: Double,
        maxTextWidth: Int
    ): View {
        val client = Minecraft.getInstance()
        val message = IslandText.parse(textValue)
        val stack = Identifier.tryParse(iconValue.trim())
            ?.let { BuiltInRegistries.ITEM.getOptional(it).orElse(null)?.defaultInstance } ?: ItemStack.EMPTY
        if (stack.item == Items.SPLASH_POTION) stack.set(
            DataComponents.POTION_CONTENTS,
            PotionContents(Potions.HEALING)
        )
        val icon = if (stack.isEmpty) null else IslandIcon(stack, actions, rockDegrees)
        val padding = if (icon == null) 24 else 46
        val lines = client.font.split(message, maxTextWidth)
        val width = ((lines.maxOfOrNull(client.font::width) ?: 0) + padding).coerceAtLeast(120)
        val height = (lines.size * 12 + 8).coerceAtLeast(if (icon == null) 22 else 24)
        val label = UIComponents.label(message).shadow(false).maxWidth(maxTextWidth).lineHeight(12).lineSpacing(0)
        label.horizontalSizing(Sizing.fixed(width - padding))
        label.color(Color.ofArgb(0))
        label.margins(Insets.left(12))
        val panel = UIContainers.horizontalFlow(Sizing.fixed(22), Sizing.fixed(height))
        panel.padding(Insets.of(4, 4, 0, 0)).verticalAlignment(VerticalAlignment.CENTER)
        icon?.let {
            it.horizontalSizing(Sizing.fixed(0)); it.verticalSizing(Sizing.fixed(0))
            panel.child(UIContainers.horizontalFlow(Sizing.fixed(16), Sizing.fixed(16)).apply {
                horizontalAlignment(HorizontalAlignment.CENTER); verticalAlignment(VerticalAlignment.CENTER)
                margins(Insets.left(8)); child(it)
            })
        }
        panel.child(label)
        val view = View(panel, label, icon, width)
        panel.surface { graphics, component ->
            val alpha = if (!view.fading) 0xE6 else (view.label.color().get().alpha() * 0xE6).toInt()
            val pose = graphics.pose()
            pose.pushMatrix(); pose.scale(0.25f, 0.25f)
            background.draw(
                graphics,
                component.x() * 4,
                component.y() * 4,
                component.width() * 4,
                component.height() * 4,
                Color.ofArgb(alpha shl 24)
            )
            pose.popMatrix()
        }
        return view
    }

    suspend fun play(
        text: String, icon: String, actions: List<String>, sounds: List<String>, holdMs: Int, rockDegrees: Double,
        onPhase: (IslandPhase) -> Unit = {}
    ) = coroutineScope {
        val client = Minecraft.getInstance()
        val iconPadding = if (icon.trim() == "none") 24 else 46
        val view =
            build(text, icon, actions, rockDegrees, (client.window.guiScaledWidth - iconPadding - 16).coerceAtLeast(1))
        val panel = view.panel
        panel.positioning(Positioning.relative(50, 0)).margins(Insets.top(8))
        try {
            Hud.add(id) { panel }
            onPhase(IslandPhase.ENTER)
            client.player?.let { player ->
                sounds.forEach { name ->
                    Identifier.tryParse(name.trim())?.let { BuiltInRegistries.SOUND_EVENT.getOptional(it).orElse(null) }
                        ?.let { player.playSound(it, 1f, 1f) }
                }
            }
            launch {
                panel.horizontalSizing().animate(360, Easing.CUBIC, Sizing.fixed(view.width + 7)).await()
                panel.horizontalSizing().animate(110, Easing.SINE, Sizing.fixed(view.width)).await()
            }
            view.icon?.let { iconView ->
                launch {
                    delay(120)
                    launch {
                        iconView.horizontalSizing().animate(190, Easing.SINE, Sizing.fixed(17)).await()
                        iconView.horizontalSizing().animate(80, Easing.SINE, Sizing.fixed(16)).await()
                    }
                    iconView.verticalSizing().animate(190, Easing.SINE, Sizing.fixed(17)).await()
                    iconView.verticalSizing().animate(80, Easing.SINE, Sizing.fixed(16)).await()
                    iconView.start()
                }
            }
            delay(200.milliseconds)
            if (view.icon != null) view.label.margins().animate(320, Easing.SINE, Insets.left(6)).forwards()
            view.label.color().animate(320, Easing.SINE, Color.WHITE).await()
            onPhase(IslandPhase.HOLD)
            delay(holdMs.coerceAtLeast(0).toLong().milliseconds)
            onPhase(IslandPhase.EXIT)
            view.icon?.stop()
            view.label.color().animate(180, Easing.SINE, Color.ofArgb(0)).await()
            view.icon?.let {
                it.horizontalSizing().animate(180, Easing.SINE, Sizing.fixed(0)).await()
                it.verticalSizing().animate(60, Easing.LINEAR, Sizing.fixed(0)).forwards()
            }
            panel.verticalSizing().animate(320, Easing.CUBIC, Sizing.fixed(22)).forwards()
            panel.horizontalSizing().animate(320, Easing.CUBIC, Sizing.fixed(22)).await()
            view.label.text(Component.empty()).color(Color.WHITE)
            view.fading = true
            view.label.color().animate(90, Easing.SINE, Color.ofArgb(0)).await()
        } finally {
            Hud.remove(id)
        }
    }

    private suspend fun Animation<*>.await() = suspendCancellableCoroutine<Unit> {
        finished().subscribe { _, _ -> if (it.isActive) it.resume(Unit) }
        forwards()
    }
}
