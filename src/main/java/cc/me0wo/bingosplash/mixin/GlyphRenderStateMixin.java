package cc.me0wo.bingosplash.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GlyphRenderState;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GlyphRenderState.class, remap = false)
public abstract class GlyphRenderStateMixin {
    @Unique
    private static final FontDescription BINGOSPLASH_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("bingosplash", "island"));

    @Shadow @Final
    private TextRenderable renderable;

    @Inject(method = "textureSetup", at = @At("HEAD"), cancellable = true)
    private void bingosplash$smoothFont(CallbackInfoReturnable<TextureSetup> cir) {
        if (renderable instanceof GlyphStyle glyph
                && BINGOSPLASH_FONT.equals(glyph.bingosplash$style().getFont())) {
            cir.setReturnValue(TextureSetup.singleTextureWithLightmap(
                    renderable.textureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
        }
    }

    @Mixin(targets = "net.minecraft.client.gui.font.glyphs.BakedSheetGlyph$GlyphInstance", remap = false)
    public interface GlyphStyle {
        @Accessor("style")
        Style bingosplash$style();
    }
}
