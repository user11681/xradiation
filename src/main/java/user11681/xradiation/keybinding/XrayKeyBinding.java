package user11681.xradiation.keybinding;

import net.minecraft.client.options.KeyBinding;

public class XrayKeyBinding extends KeyBinding {
    public XrayKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    @Override
    public void setPressed(boolean pressed) {
        if (!this.isPressed() && pressed) {
            this.onPress();
        }

        super.setPressed(pressed);
    }

    protected void onPress() {}
}
