package user11681.xray;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user11681.xray.keybinding.XrayScreenKey;
import user11681.xray.keybinding.XrayToggleKey;

@Environment(EnvType.CLIENT)
public class Main implements ClientModInitializer {
    public static final String MOD_ID = "xray";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final KeyBinding TOGGLE_XRAY = new XrayToggleKey();
    public static final KeyBinding XRAY_SCREEN = new XrayScreenKey();

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_XRAY);
        KeyBindingHelper.registerKeyBinding(XRAY_SCREEN);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "nether_portal"), XrayItems.NETHER_PORTAL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "end_portal"), XrayItems.END_PORTAL);

        XrayItems.NETHER_PORTAL.appendBlocks(Item.BLOCK_ITEMS, XrayItems.NETHER_PORTAL);
        XrayItems.END_PORTAL.appendBlocks(Item.BLOCK_ITEMS, XrayItems.END_PORTAL);

        Configuration.INSTANCE.read();
    }

    public static PlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }
}
