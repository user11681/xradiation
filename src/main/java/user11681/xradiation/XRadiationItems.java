package user11681.xradiation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class XRadiationItems {
    public static final List<Item> ITEMS = new ObjectArrayList<>();

    public static final BlockItem NETHER_PORTAL = new BlockItem(Blocks.NETHER_PORTAL, new Item.Settings());
    public static final BlockItem END_PORTAL = new BlockItem(Blocks.END_PORTAL, new Item.Settings());

    public static void register() {
        register(NETHER_PORTAL);
        register(END_PORTAL);
        ITEMS.add(Items.SPAWNER);
    }

    private static void register(final BlockItem item) {
        ITEMS.add(Registry.register(Registry.ITEM, new Identifier(Main.MOD_ID, Registry.BLOCK.getId(item.getBlock()).getPath()), item));
    }
}
