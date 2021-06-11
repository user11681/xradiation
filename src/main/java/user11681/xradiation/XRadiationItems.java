package user11681.xradiation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

public class XRadiationItems {
    public static final List<Item> ITEMS = new ObjectArrayList<>();

    public static final BlockItem netherPortal = new BlockItem(Blocks.NETHER_PORTAL, new Item.Settings());
    public static final BlockItem endPortal = new BlockItem(Blocks.END_PORTAL, new Item.Settings());

    public static void register() {
        register(netherPortal);
        register(endPortal);

        ITEMS.add(Items.SPAWNER);
    }

    private static void register(BlockItem item) {
        ITEMS.add(Registry.register(Registry.ITEM, XRadiation.id(Registry.BLOCK.getId(item.getBlock()).getPath()), item));
    }
}
