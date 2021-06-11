package user11681.xradiation.keybinding;

import org.lwjgl.glfw.GLFW;
import user11681.xradiation.Configuration;

public class XrayToggleKey extends XrayKeyBinding {
    public XrayToggleKey() {
        super("key.xray.toggle", GLFW.GLFW_KEY_X, "X-ray");
    }

    @Override
    protected void onPress() {
        Configuration.instance.toggle();
        Configuration.instance.write();
        Configuration.instance.reload();
    }
}
