package user11681.xradiation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user11681.xradiation.keybinding.XrayScreenKey;
import user11681.xradiation.keybinding.XrayToggleKey;

public class Main implements ClientModInitializer {
    public static final String MOD_ID = "xradiation";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final KeyBinding TOGGLE_XRAY = new XrayToggleKey();
    public static final KeyBinding XRAY_SCREEN = new XrayScreenKey();

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_XRAY);
        KeyBindingHelper.registerKeyBinding(XRAY_SCREEN);

        XRadiationItems.register();
        XRadiationItems.NETHER_PORTAL.appendBlocks(Item.BLOCK_ITEMS, XRadiationItems.NETHER_PORTAL);
        XRadiationItems.END_PORTAL.appendBlocks(Item.BLOCK_ITEMS, XRadiationItems.END_PORTAL);

        Configuration.INSTANCE.read();
    }

    public static PlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }
}
