package user11681.xray.mixin.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.BlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import user11681.xray.Configuration;

@Environment(EnvType.CLIENT)
@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {
    private static final String render = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JI)Z";

    @ModifyVariable(method = render, at = @At("LOAD"), ordinal = 1)
    private boolean renderFlat(final boolean renderSmooth) {
        return renderSmooth && !Configuration.INSTANCE.enabled;
    }
}
