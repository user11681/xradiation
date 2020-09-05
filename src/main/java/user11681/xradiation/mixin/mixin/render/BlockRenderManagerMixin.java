package user11681.xradiation.mixin.mixin.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.Configuration;

@Mixin(BlockRenderManager.class)
abstract class BlockRenderManagerMixin {
    @Inject(method = "renderFluid", at = @At("HEAD"), cancellable = true)
    private void filterFluids(final BlockPos pos, final BlockRenderView blockRenderView, final VertexConsumer vertexConsumer, final FluidState state, final CallbackInfoReturnable<Boolean> info) {
        if (Configuration.INSTANCE.shouldFilter(state)) {
            info.setReturnValue(false);
        }
    }
}
