package user11681.xradiation.mixin.mixin.render.indigo;

import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.Configuration;

@Mixin(AbstractQuadRenderer.class)
abstract class AbstractQuadRendererMixin {
    @Shadow(remap = false)
    @Final
    static int FULL_BRIGHTNESS;

    @Inject(method = "flatBrightness", at = @At("HEAD"), cancellable = true, remap = false)
    private void maximizeBrightness(MutableQuadViewImpl quad, BlockState blockState, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        if (Configuration.instance.enabled) {
            info.setReturnValue(FULL_BRIGHTNESS);
        }
    }
}
