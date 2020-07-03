package user11681.xray.gui.widget;

import net.minecraft.util.Identifier;
import user11681.xray.Main;

public class CrossButton extends SquareTexturedButton {
    public static final Identifier CROSS_TEXTURE = new Identifier(Main.MOD_ID, "textures/cross.png");

    public CrossButton(final int x, final int y, final PressAction onPress) {
        super(x, y, CROSS_TEXTURE, onPress);

        this.g = 0x44 / 255F;
        this.b = 0;
    }
}
