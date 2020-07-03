package user11681.xray.mixin.mixin.render.indigo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.indigo.renderer.RenderMaterialImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xray.Configuration;

@Environment(EnvType.CLIENT)
@Mixin(RenderMaterialImpl.class)
public abstract class RenderMaterialImplMixin {
    @Inject(method = "disableAo", at = @At("HEAD"), cancellable = true, remap = false)
    private void flatten(final int textureIndex, final CallbackInfoReturnable<Boolean> info) {
        if (Configuration.INSTANCE.enabled) {
            info.setReturnValue(true);
        }
    }
}
