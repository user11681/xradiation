package user11681.xray.mixin.duck;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public interface WorldRendererDuck {
    Map<BlockPos, BlockState> OUTLINED_STATES = new Object2ObjectOpenHashMap<>();

    void drawOutline(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices);
}
