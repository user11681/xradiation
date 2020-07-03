package user11681.xray.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.fluid.Fluid;

public class FluidData {
    public static final Map<Fluid, Set<Fluid>> FLUIDS = new Object2ObjectOpenHashMap<>(5);
}
