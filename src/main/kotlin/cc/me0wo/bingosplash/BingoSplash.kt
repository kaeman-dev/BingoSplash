package cc.me0wo.bingosplash

import cc.me0wo.bingosplash.command.SplashCommand
import cc.me0wo.bingosplash.config.BingoSplashConfigWrapper
import net.fabricmc.api.ClientModInitializer

object BingoSplash : ClientModInitializer {
    lateinit var config: BingoSplashConfigWrapper
        private set

    override fun onInitializeClient() {
        config = BingoSplashConfigWrapper.createAndLoad()
        SplashCommand.register()
    }
}
