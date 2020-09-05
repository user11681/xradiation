package user11681.xradiation.keybinding;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;
import user11681.xradiation.Configuration;

@Environment(EnvType.CLIENT)
public class XrayToggleKey extends XrayKeyBinding {
    public XrayToggleKey() {
        super("key.xray.toggle", GLFW.GLFW_KEY_X, "X-ray");
    }

    @Override
    protected void onPress() {
        Configuration.INSTANCE.toggle();
        Configuration.INSTANCE.write();
        Configuration.INSTANCE.reload();
    }
}
