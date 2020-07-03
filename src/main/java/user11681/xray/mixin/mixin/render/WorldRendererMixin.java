package user11681.xray.mixin.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import user11681.xray.mixin.duck.WorldRendererDuck;


@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererDuck {
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private static void drawShapeOutline(final MatrixStack matrixStack, final VertexConsumer vertexConsumer, final VoxelShape voxelShape, final double d,
                                         final double e, final double f, final float g, final float h, final float i, final float j) {
    }

    @Override
    @Unique
    public void drawOutline(final BlockState state, final BlockPos pos, final BlockRenderView world, final MatrixStack matrices) {
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
