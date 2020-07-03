package user11681.xray.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BlockItemData {
    public static final Map<Item, Set<Block>> ITEM_BLOCKS = new Object2ObjectOpenHashMap<>();
}
