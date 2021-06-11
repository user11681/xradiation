package user11681.xradiation.mixin.mixin.render;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.Configuration;

@Mixin(BlockEntityRenderDispatcher.class)
abstract class BlockEntityRenderDispatcherMixin {
    private static final String render = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V";

    @Inject(method = render, at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void filterBlockEntities(BlockEntityRenderer<T> renderer, T entity, float tickDelta,
                                                                    final MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
        if (Configuration.instance.shouldFilter(entity)) {
            info.cancel();
        }
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void filterBlockEntities(E entity, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider,
                                                             final int light, int overlay, CallbackInfoReturnable<Boolean> info) {
        if (Configuration.instance.shouldFilter(entity)) {
            info.cancel();
        }
    }
}
