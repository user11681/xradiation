package user11681.xradiation.mixin.mixin.render.indigo;

import net.fabricmc.fabric.impl.client.indigo.renderer.RenderMaterialImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.Configuration;

@Mixin(RenderMaterialImpl.class)
abstract class RenderMaterialImplMixin {
    @Inject(method = "disableAo", at = @At("HEAD"), cancellable = true, remap = false)
    private void flatten(int textureIndex, CallbackInfoReturnable<Boolean> info) {
        if (Configuration.instance.enabled) {
            info.setReturnValue(true);
        }
    }
}
