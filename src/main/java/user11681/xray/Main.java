package user11681.xray;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Main implements ClientModInitializer {
    public static final String MOD_ID = "xray";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final KeyBinding TOGGLE_XRAY = new XrayKeyBinding();

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_XRAY);
        Configuration.INSTANCE.read();
    }
}
