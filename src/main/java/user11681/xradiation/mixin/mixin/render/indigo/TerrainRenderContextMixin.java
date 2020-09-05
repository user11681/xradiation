package user11681.xradiation.mixin.mixin.render.indigo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.Configuration;

@Mixin(TerrainRenderContext.class)
abstract class TerrainRenderContextMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void filterBlock(final BlockState blockState, final BlockPos blockPos, final BakedModel model, final MatrixStack matrixStack, final CallbackInfoReturnable<Boolean> info) {
        if (Configuration.INSTANCE.shouldFilter(blockState)) {
            info.setReturnValue(false);
        }
    }
}
