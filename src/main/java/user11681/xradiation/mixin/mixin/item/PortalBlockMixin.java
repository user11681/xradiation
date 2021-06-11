package user11681.xradiation.mixin.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({NetherPortalBlock.class, EndPortalBlock.class})
abstract class PortalBlockMixin extends Block {
    public PortalBlockMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "getPickStack", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;EMPTY:Lnet/minecraft/item/ItemStack;"))
    public ItemStack allowPickBlock(BlockView world, BlockPos pos, BlockState state) {
        return super.getPickStack(world, pos, state);
    }
}
