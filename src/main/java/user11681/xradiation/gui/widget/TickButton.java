package user11681.xradiation.gui.widget;

import net.minecraft.util.Identifier;
import user11681.xradiation.XRadiation;

public class TickButton extends SquareTexturedButton {
    public static final Identifier TICK_TEXTURE = XRadiation.id("textures/tick.png");

    public TickButton(int x, int y, PressAction onPress) {
        super(x, y, TICK_TEXTURE, onPress);

        this.r = 0xA0 / 255F;
        this.b = 0xA0 / 255F;
    }
}
