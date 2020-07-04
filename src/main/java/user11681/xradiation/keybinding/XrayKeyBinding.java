package user11681.xradiation.keybinding;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;

@Environment(EnvType.CLIENT)
public class XrayKeyBinding extends KeyBinding {
    public XrayKeyBinding(final String translationKey, final int code, final String category) {
        super(translationKey, code, category);
    }

    @Override
    public void setPressed(final boolean pressed) {
        if (!this.isPressed() && pressed) {
            this.onPress();
        }

        super.setPressed(pressed);
    }

    protected void onPress() {}
}
