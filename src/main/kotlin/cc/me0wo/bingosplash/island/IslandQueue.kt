package cc.me0wo.bingosplash.island

import cc.me0wo.bingosplash.BingoSplash
import cc.me0wo.bingosplash.island.render.IslandHud
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong

enum class IslandPhase { IDLE, ENTER, HOLD, EXIT }

object IslandQueue {
    data class Status(
        val current: Long? = null,
        val phase: IslandPhase = IslandPhase.IDLE,
        val waiting: Int = 0,
        val received: Long = 0,
        val completed: Long = 0,
        val failed: Long = 0,
        val startedAt: Long = 0
    ) {
        val elapsedMs get() = if (current == null) 0 else Util.getMillis() - startedAt
    }

    @Volatile
    var status = Status()
        private set

    private val ids = AtomicLong()
    private val gate = Mutex()
    private val scope = CoroutineScope(Minecraft.getInstance().asCoroutineDispatcher() + SupervisorJob())
    private val logger = LoggerFactory.getLogger("BingoSplash")

    fun submit(text: String? = null): Long {
        val config = BingoSplash.config
        val snapshotText = text ?: config.text()
        val snapshotIcon = config.icon()
        val snapshotActions = config.actions().toList()
        val snapshotSounds = config.sounds().toList()
        val snapshotHold = config.holdMs()
        val snapshotRock = config.rockDegrees()
        val id = ids.incrementAndGet()
        status = status.copy(received = id, waiting = status.waiting + 1)

        scope.launch {
            gate.withLock {
                status = status.copy(
                    current = id,
                    waiting = (status.waiting - 1).coerceAtLeast(0),
                    startedAt = Util.getMillis()
                )
                try {
                    IslandHud.play(
                        snapshotText,
                        snapshotIcon,
                        snapshotActions,
                        snapshotSounds,
                        snapshotHold,
                        snapshotRock
                    ) { phase ->
                        status = status.copy(phase = phase)
                        if (config.debug()) logger.info(
                            "Island #{}: {} (waiting={}, elapsed={}ms)",
                            id,
                            phase,
                            status.waiting,
                            status.elapsedMs
                        )
                    }
                    status = status.copy(completed = status.completed + 1)
                    if (config.debug()) logger.info("Island #{} completed in {}ms", id, status.elapsedMs)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    status = status.copy(failed = status.failed + 1)
                    logger.error("Island #$id failed after ${status.elapsedMs}ms", exception)
                } finally {
                    status = status.copy(current = null, phase = IslandPhase.IDLE, startedAt = 0)
                }
            }
        }
        return id
    }
}
