package user11681.xray.keybinding;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import user11681.xray.gui.screen.XrayScreen;

public class XrayScreenKey extends XrayKeyBinding {
    public XrayScreenKey() {
        super("key.xray.screen", GLFW.GLFW_KEY_V, "X-ray");
    }

    @Override
    protected void onPress() {
        MinecraftClient.getInstance().openScreen(new XrayScreen());
    }
}
