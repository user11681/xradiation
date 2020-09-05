package user11681.xradiation.registry;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
public class BlockItemData {
    public static final Reference2ReferenceOpenHashMap<Item, ReferenceOpenHashSet<Block>> ITEM_BLOCKS = new Reference2ReferenceOpenHashMap<>();
}
