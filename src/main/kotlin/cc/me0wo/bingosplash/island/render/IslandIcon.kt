package cc.me0wo.bingosplash.island.render

import io.wispforest.owo.ui.component.ItemComponent
import io.wispforest.owo.ui.core.OwoUIGraphics
import net.minecraft.util.Util
import net.minecraft.world.item.ItemStack
import org.joml.Matrix3x2fStack
import java.util.*
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

internal class IslandIcon(stack: ItemStack, actions: List<String>, rockDegrees: Double) : ItemComponent(stack) {
    private val animations = actions.mapNotNull { ACTIONS[it.trim().lowercase(Locale.ROOT)] }
    private val amplitude = Math.toRadians(rockDegrees.coerceIn(0.0, 45.0)).toFloat()
    private var startedAt: Long? = null

    fun start() {
        startedAt = Util.getMillis()
    }

    fun stop() {
        startedAt = null
    }

    override fun draw(graphics: OwoUIGraphics, mouseX: Int, mouseY: Int, a: Float, b: Float) {
        val pose = graphics.pose()
        pose.pushMatrix()
        try {
            startedAt?.let { start ->
                val x = x() + width() / 2f
                val y = y() + height() / 2f
                pose.translate(x, y)
                animations.forEach { it(pose, (Util.getMillis() - start) / 1000.0, amplitude) }
                pose.translate(-x, -y)
            }
            super.draw(graphics, mouseX, mouseY, a, b)
        } finally {
            pose.popMatrix()
        }
    }

    companion object {
        private val ACTIONS: Map<String, (Matrix3x2fStack, Double, Float) -> Unit> = mapOf(
            "rock" to { pose, elapsed, amplitude ->
                val phase = elapsed % 1.0
                if (phase < 0.75) pose.rotate((sin(phase * 8 * PI) * amplitude * exp(-1.5 * phase)).toFloat())
            }
        )
    }
}
