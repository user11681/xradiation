package user11681.xray;

import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class XrayItems {
    public static final BlockItem NETHER_PORTAL = new BlockItem(Blocks.NETHER_PORTAL, new Item.Settings().group(ItemGroup.DECORATIONS));
    public static final BlockItem END_PORTAL = new BlockItem(Blocks.END_PORTAL, new Item.Settings().group(ItemGroup.DECORATIONS));
}
