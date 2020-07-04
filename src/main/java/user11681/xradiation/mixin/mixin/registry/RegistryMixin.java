package user11681.xradiation.mixin.mixin.registry;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.registry.FluidData;

@Mixin(Registry.class)
public abstract class RegistryMixin {
    @Inject(method = "register(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"))
    private static <V, T extends V> void registerBuckets(final Registry<V> registry, final Identifier id, final T entry, final CallbackInfoReturnable<T> cir) {
        if (entry instanceof Fluid) {
            final Fluid fluid = (Fluid) entry;
            final Set<Fluid> newFluids = new ObjectLinkedOpenHashSet<>();
            final Map<Fluid, Set<Fluid>> fluids = FluidData.FLUIDS;

            newFluids.add(fluid);

            for (final Fluid otherFluid : fluids.keySet()) {
                if (fluid.matchesType(otherFluid)) {
                    fluids.get(otherFluid).add(fluid);
                    newFluids.add(otherFluid);
                }
            }

            FluidData.FLUIDS.put(fluid, newFluids);
        }
    }
}
