package user11681.xradiation.mixin.mixin.registry;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import user11681.xradiation.registry.RegistryData;

@Mixin(Registry.class)
abstract class RegistryMixin {
    @Inject(method = "register(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"))
    private static <V, T extends V> void registerBuckets(final Registry<V> registry, final Identifier id, final T entry, final CallbackInfoReturnable<T> cir) {
        if (entry instanceof Fluid) {
            final Fluid fluid = (Fluid) entry;
            final ReferenceLinkedOpenHashSet<Fluid> newFluids = new ReferenceLinkedOpenHashSet<>();
            final Reference2ReferenceOpenHashMap<Fluid, ReferenceLinkedOpenHashSet<Fluid>> fluids = RegistryData.FLUIDS;

            newFluids.add(fluid);

            for (final Fluid otherFluid : fluids.keySet()) {
                if (fluid.matchesType(otherFluid)) {
                    fluids.get(otherFluid).add(fluid);
                    newFluids.add(otherFluid);
                }
            }

            RegistryData.FLUIDS.put(fluid, newFluids);
        }
    }
}
