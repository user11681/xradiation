package user11681.xradiation.gui.widget;

import net.minecraft.util.Identifier;
import user11681.xradiation.Main;

public class TickButton extends SquareTexturedButton {
    public static final Identifier TICK_TEXTURE = new Identifier(Main.MOD_ID, "textures/tick.png");

    public TickButton(final int x, final int y, final PressAction onPress) {
        super(x, y, TICK_TEXTURE, onPress);

        this.r = 0xA0 / 255F;
        this.b = 0xA0 / 255F;
    }
}
