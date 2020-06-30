package user11681.xray.mixin.mixin;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xray.Configuration;

@Mixin(BlockRenderManager.class)
public abstract class BlockRenderManagerMixin {
    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void blockRender(final BlockState state, final BlockPos pos, final BlockRenderView world, final MatrixStack matrix,
                             final VertexConsumer vertexConsumer, final boolean cull, final Random random, final CallbackInfoReturnable<Boolean> info) {
        if (Configuration.INSTANCE.enabled && !Configuration.INSTANCE.allowedBlocks.contains(state.getBlock())) {
            info.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "renderBlock", at = @At("LOAD"), ordinal = 0)
    private boolean cull(final boolean cull) {
        return cull && !Configuration.INSTANCE.enabled;
    }
}
