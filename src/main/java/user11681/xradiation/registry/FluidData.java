package user11681.xradiation.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;

@Environment(EnvType.CLIENT)
public class FluidData {
    public static final Map<Fluid, Set<Fluid>> FLUIDS = new Object2ObjectOpenHashMap<>(5);
}
