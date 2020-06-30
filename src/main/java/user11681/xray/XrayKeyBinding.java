package user11681.xray;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class XrayKeyBinding extends KeyBinding {
    public XrayKeyBinding() {
        super("key.xray.toggle", GLFW.GLFW_KEY_X, "X-ray");
    }

    @Override
    public void setPressed(final boolean pressed) {
        if (!this.isPressed() && pressed) {
            Configuration.INSTANCE.toggle();
            MinecraftClient.getInstance().worldRenderer.reload();
        }

        super.setPressed(pressed);
    }
}
