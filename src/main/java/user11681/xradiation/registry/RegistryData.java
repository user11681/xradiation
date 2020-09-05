package user11681.xradiation.registry;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

public class RegistryData {
    public static final Reference2ReferenceOpenHashMap<Item, ReferenceOpenHashSet<Block>> ITEM_BLOCKS = new Reference2ReferenceOpenHashMap<>();
    public static final Reference2ReferenceOpenHashMap<Fluid, ReferenceLinkedOpenHashSet<Fluid>> FLUIDS = new Reference2ReferenceOpenHashMap<>(5);
}
