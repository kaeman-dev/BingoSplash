package cc.me0wo.bingosplash.config.ui

import cc.me0wo.bingosplash.BingoSplash
import io.wispforest.owo.config.ui.ConfigScreen
import io.wispforest.owo.ui.container.FlowLayout
import net.minecraft.client.gui.screens.Screen

class SplashConfigScreen(parent: Screen?) : ConfigScreen(ConfigScreen.DEFAULT_MODEL_ID, BingoSplash.config, parent) {
    override fun build(root: FlowLayout) {
        super.build(root)
        ConfigTheme.apply(root)
    }
}
