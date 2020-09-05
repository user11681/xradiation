package user11681.xradiation.mixin.mixin.render.indigo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainBlockRenderInfo;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.Configuration;

@Mixin(TerrainBlockRenderInfo.class)
abstract class TerrainBlockRenderInfoMixin {
    @Inject(method = "shouldDrawFace", at = @At("HEAD"), cancellable = true, remap = false)
    private void forceDrawFace(final Direction face, final CallbackInfoReturnable<Boolean> info) {
        if (Configuration.INSTANCE.enabled) {
            info.setReturnValue(true);
        }
    }
}
