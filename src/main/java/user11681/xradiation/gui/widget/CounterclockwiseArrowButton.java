package user11681.xradiation.gui.widget;

import net.minecraft.util.Identifier;
import user11681.xradiation.XRadiation;

public class CounterclockwiseArrowButton extends SquareTexturedButton {
    public static final Identifier COUNTERCLOCKWISE_ARROW_TEXTURE = XRadiation.id("textures/counterclockwise_arrow.png");

    public CounterclockwiseArrowButton(int x, int y, PressAction onPress) {
        super(x, y, COUNTERCLOCKWISE_ARROW_TEXTURE, onPress);

        this.r = 0;
        this.g = 0;
        this.b = 0;
    }
}
