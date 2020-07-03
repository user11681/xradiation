package user11681.xray.keybinding;

import org.lwjgl.glfw.GLFW;
import user11681.xray.Configuration;

public class XrayToggleKey extends XrayKeyBinding {
    public XrayToggleKey() {
        super("key.xray.toggle", GLFW.GLFW_KEY_X, "X-ray");
    }

    @Override
    protected void onPress() {
        Configuration.INSTANCE.toggle();
        Configuration.INSTANCE.reload();
    }
}
