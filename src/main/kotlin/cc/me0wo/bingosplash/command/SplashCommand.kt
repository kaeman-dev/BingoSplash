package cc.me0wo.bingosplash.command

import cc.me0wo.bingosplash.BingoSplash
import cc.me0wo.bingosplash.config.ui.SplashConfigScreen
import cc.me0wo.bingosplash.island.IslandQueue
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import java.util.*

object SplashCommand {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("bingosplash").executes {
                    it.source.client.schedule {
                        it.source.client.setScreenAndShow(SplashConfigScreen(null))
                    }
                    1
                }
                    .then(
                        literal("island").executes { enqueue(it.source) }
                            .then(
                                argument("text", StringArgumentType.greedyString()).executes {
                                    enqueue(it.source, StringArgumentType.getString(it, "text"))
                                }
                            )
                    )
            )
        }
    }

    private fun enqueue(source: FabricClientCommandSource, text: String? = null): Int {
        if (!BingoSplash.config.enabled()) {
            source.sendError(Component.translatable("message.bingosplash.disabled"))
            return 0
        }
        val id = IslandQueue.submit(text)
        val state = IslandQueue.status
        source.sendFeedback(
            Component.translatable(
            "message.bingosplash.queued", id, state.waiting,
            state.current?.let { Component.literal("#$it") } ?: Component.translatable("ui.bingosplash.none"),
            Component.translatable("ui.bingosplash.phase.${state.phase.name.lowercase(Locale.ROOT)}")
        ).append("\n").append(
            Component.translatable(
                "message.bingosplash.queue_stats", state.received, state.completed, state.failed, state.elapsedMs
            )
        ))
        return 1
    }
}
