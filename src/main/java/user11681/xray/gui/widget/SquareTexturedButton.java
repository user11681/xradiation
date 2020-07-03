package user11681.xray.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class SquareTexturedButton extends ButtonWidget {
    private static final TextureManager TEXTURE_MANAGER = MinecraftClient.getInstance().getTextureManager();

    public final Identifier texture;

    public float r;
    public float g;
    public float b;
    public float a;

    public SquareTexturedButton(final int x, final int y, final Identifier texture, final PressAction onPress) {
        super(x, y, 20, 20, LiteralText.EMPTY, onPress);

        this.texture = texture;

        this.r = 1;
        this.g = 1;
        this.b = 1;
        this.a = 1;
    }

    @Override
    public void render(final MatrixStack matrices, final int mouseX, final int mouseY, final float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        TEXTURE_MANAGER.bindTexture(this.texture);

        RenderSystem.color4f(this.r, this.g, this.b, this.active ? this.a : this.a / 2);

        drawTexture(matrices, this.x + 2, this.y + 2, 0, 0, 16, 16, 16, 16);
    }
}
