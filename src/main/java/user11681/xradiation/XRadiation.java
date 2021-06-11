package user11681.xradiation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user11681.xradiation.keybinding.XrayScreenKey;
import user11681.xradiation.keybinding.XrayToggleKey;

public class XRadiation implements ClientModInitializer {
    public static final String ID = "xradiation";

    public static final Logger logger = LogManager.getLogger(ID);

    public static final KeyBinding xrayToggle = new XrayToggleKey();
    public static final KeyBinding xrayScreen = new XrayScreenKey();

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(xrayToggle);
        KeyBindingHelper.registerKeyBinding(xrayScreen);

        XRadiationItems.register();
        XRadiationItems.netherPortal.appendBlocks(Item.BLOCK_ITEMS, XRadiationItems.netherPortal);
        XRadiationItems.endPortal.appendBlocks(Item.BLOCK_ITEMS, XRadiationItems.endPortal);

        Configuration.instance.read();
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    public static PlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }
}
