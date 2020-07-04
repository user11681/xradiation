package user11681.xradiation.mixin.duck;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;

@Environment(EnvType.CLIENT)
public interface BucketFluidAccessor {
    Fluid getFluid();
}
