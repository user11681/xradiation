package user11681.xradiation.mixin.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import user11681.xradiation.mixin.duck.WorldRendererDuck;


@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin implements WorldRendererDuck {
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private static void drawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d,
                                         final double e, double f, float g, float h, float i, float j) {
    }

    @Override
    @Unique
    public void drawOutline(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices) {
        RenderSystem.recordRenderCall(() -> {
            final Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

            drawShapeOutline(matrices, this.bufferBuilders.getOutlineVertexConsumers().getBuffer(RenderLayer.LINES), state.getOutlineShape(world, pos),
                    pos.getX() - camera.x,
                    pos.getY() - camera.y,
                    pos.getZ() - camera.z,
                    1, 1, 1, 1F
            );
        });
    }
}
