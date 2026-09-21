package cc.me0wo.bingosplash.config

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.RangeConstraint
import io.wispforest.owo.config.annotation.SectionHeader

@Config(name = "bingosplash", wrapperName = "BingoSplashConfigWrapper", saveOnModification = true)
class BingoSplashConfigModel {
    @field:SectionHeader("general")
    @JvmField
    var enabled = true
    @JvmField
    var text = "Hello World"
    @JvmField
    var icon = "minecraft:splash_potion"
    @JvmField
    var actions: List<String> = listOf("rock")
    @JvmField
    var sounds: List<String> = listOf(
        "minecraft:entity.splash_potion.break",
        "minecraft:block.bell.use"
    )
    @JvmField
    var debug = false

    @field:SectionHeader("animation")
    @field:RangeConstraint(min = 500.0, max = 8000.0)
    @JvmField
    var holdMs = 2600

    @field:RangeConstraint(min = 0.0, max = 45.0)
    @JvmField
    var rockDegrees = 15.0
}
