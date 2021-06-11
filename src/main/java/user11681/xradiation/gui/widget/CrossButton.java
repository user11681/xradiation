package user11681.xradiation.gui.widget;

import net.minecraft.util.Identifier;
import user11681.xradiation.XRadiation;

public class CrossButton extends SquareTexturedButton {
    public static final Identifier CROSS_TEXTURE = XRadiation.id("textures/cross.png");

    public CrossButton(int x, int y, PressAction onPress) {
        super(x, y, CROSS_TEXTURE, onPress);

        this.g = 0x44 / 255F;
        this.b = 0;
    }
}
