package user11681.xradiation.mixin.mixin.item;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import user11681.xradiation.mixin.duck.BucketFluidAccessor;

@Mixin(BucketItem.class)
abstract class BucketItemMixin implements BucketFluidAccessor {
    @Override
    @Accessor
    public abstract Fluid getFluid();
}
