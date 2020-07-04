package user11681.xradiation.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
public class BlockItemData {
    public static final Map<Item, Set<Block>> ITEM_BLOCKS = new Object2ObjectOpenHashMap<>();
}
