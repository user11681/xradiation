package user11681.xradiation.gui.widget;

import net.minecraft.util.Identifier;
import user11681.xradiation.Main;

public class CounterclockwiseArrowButton extends SquareTexturedButton {
    public static final Identifier COUNTERCLOCKWISE_ARROW_TEXTURE = new Identifier(Main.MOD_ID, "textures/counterclockwise_arrow.png");

    public CounterclockwiseArrowButton(final int x, final int y, final PressAction onPress) {
        super(x, y, COUNTERCLOCKWISE_ARROW_TEXTURE, onPress);

        this.r = 0;
        this.g = 0;
        this.b = 0;
    }
}
