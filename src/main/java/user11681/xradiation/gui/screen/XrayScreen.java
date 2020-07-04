package user11681.xradiation.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.class_5414;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import user11681.xradiation.Configuration;
import user11681.xradiation.Main;
import user11681.xradiation.XRadiationItems;
import user11681.xradiation.gui.widget.CounterclockwiseArrowButton;
import user11681.xradiation.gui.widget.CrossButton;
import user11681.xradiation.gui.widget.SquareTexturedButton;
import user11681.xradiation.gui.widget.TickButton;

@Environment(EnvType.CLIENT)
public class XrayScreen extends AbstractInventoryScreen<XrayScreen.XrayScreenHandler> {
    public static final Identifier TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final SimpleInventory inventory = new SimpleInventory(45);
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final List<ItemGroup> GROUPS = new ArrayList<>(Arrays.asList(ItemGroup.GROUPS));
    private static final String ENABLED_KEY = String.format("%s:enabled", Main.MOD_ID);
    private static final String DISABLED_KEY = String.format("%s:disabled", Main.MOD_ID);
    private static final String ALLOWED_KEY = String.format("%s:allowed", Main.MOD_ID);
    private static final String DISALLOWED_KEY = String.format("%s:disallowed", Main.MOD_ID);
    private static int selectedTab = GROUPS.indexOf(ItemGroup.BUILDING_BLOCKS);

    static {
        GROUPS.remove(ItemGroup.INVENTORY);
        GROUPS.remove(ItemGroup.HOTBAR);
    }

    private final PlayerEntity player;
    private final Map<Identifier, Tag<Item>> searchResultTags;
    private boolean scrolling;
    private boolean ignoreTypedCharacter;
    private float scrollPosition;
    private TextFieldWidget searchBox;
    private CreativeInventoryListener listener;
    private SquareTexturedButton applyButton;
    private SquareTexturedButton discardButton;
    private SquareTexturedButton resetButton;

    public XrayScreen() {
        super(new XrayScreenHandler(), Main.getPlayer().inventory, LiteralText.EMPTY);

        this.player = Main.getPlayer();
        this.player.currentScreenHandler = this.handler;
        this.searchResultTags = new TreeMap<>();
        this.passEvents = true;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
    }

    private static void fill(final Slot slot, final int color) {
        fill(new MatrixStack(), slot.x, slot.y, slot.x + 16, slot.y + 16, color);
    }

    @Override
    protected void init() {
        super.init();

        client.keyboard.enableRepeatEvents(true);

        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setHasBorder(false);
        this.searchBox.setVisible(false);
        this.searchBox.setEditableColor(0x888888);
        this.children.add(this.searchBox);
        this.applyButton = this.addButton(new TickButton(this.x + 8, this.y + 110, (final ButtonWidget button) -> {
            Configuration.INSTANCE.apply();

            this.updateButtons();
        }));
        this.discardButton = this.addButton(new CrossButton(this.x + 32, this.y + 110, (final ButtonWidget button) -> {
            Configuration.INSTANCE.discard();

            this.updateButtons();
        }));
        this.resetButton = this.addButton(new CounterclockwiseArrowButton(this.x + 56, this.y + 110, (final ButtonWidget button) -> {
            Configuration.INSTANCE.reset();

            this.updateButtons();
        }));
        this.updateButtons();

        final int i = selectedTab;

        selectedTab = -1;

        this.setSelectedTab(GROUPS.get(i));
        this.player.playerScreenHandler.removeListener(this.listener);
        this.listener = new CreativeInventoryListener(client);
        this.player.playerScreenHandler.addListener(this.listener);
    }

    @Override
    public void tick() {
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    private boolean isValidSlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == inventory;
    }

    @Override
    protected void applyStatusEffectOffset() {
        final int x = this.x;

        super.applyStatusEffectOffset();

        if (this.searchBox != null && this.x != x) {
            this.searchBox.setX(this.x + 82);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }

    }

    @Override
    public void removed() {
        super.removed();
        if (this.player != null && this.player.inventory != null) {
            this.player.playerScreenHandler.removeListener(this.listener);
        }

        client.keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (!this.ignoreTypedCharacter && selectedTab == GROUPS.indexOf(ItemGroup.SEARCH)) {
            final String string = this.searchBox.getText();

            if (this.searchBox.charTyped(chr, keyCode)) {
                if (!Objects.equals(string, this.searchBox.getText())) {
                    this.search();
                }

                return true;
            }

            return false;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (selectedTab != GROUPS.indexOf(ItemGroup.SEARCH)) {
            if (client.options.keyChat.matchesKey(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(ItemGroup.SEARCH);
                return true;

            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            boolean bl = !this.isValidSlot(this.focusedSlot) || this.focusedSlot.hasStack();
            boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).method_30103().isPresent();
            if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                String string = this.searchBox.getText();
                if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                    if (!Objects.equals(string, this.searchBox.getText())) {
                        this.search();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        final DefaultedList<ItemStack> itemList = this.handler.itemList;

        itemList.clear();
        this.searchResultTags.clear();
        String string = this.searchBox.getText();

        if (string.isEmpty()) {
            for (final Item item : Item.BLOCK_ITEMS.values()) {
                item.appendStacks(ItemGroup.SEARCH, itemList);
            }
        } else {
            final SearchableContainer<ItemStack> container;

            this.addCustomItems(string);

            if (ALLOWED_KEY.startsWith(string) || ENABLED_KEY.startsWith(string)) {
                itemList.addAll(Configuration.INSTANCE.allowedBlocks.parallelStream().map((final Block block) -> block.asItem().getStackForRender()).collect(Collectors.toList()));
                itemList.addAll(Configuration.INSTANCE.allowedFluids.parallelStream().map((final Fluid fluid) -> fluid.getBucketItem().getStackForRender()).collect(Collectors.toList()));
            } else if (DISALLOWED_KEY.startsWith(string) || DISABLED_KEY.startsWith(string)) {
                for (final Item item : Item.BLOCK_ITEMS.values()) {
                    item.appendStacks(ItemGroup.SEARCH, itemList);
                }

                itemList.removeIf(Configuration.INSTANCE::isAllowed);
            } else {
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    container = client.getSearchableContainer(SearchManager.ITEM_TAG);
                    this.searchForTags(string);
                } else {
                    container = client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
                }

                final List<ItemStack> items = container.findAll(string.toLowerCase(Locale.ROOT)).parallelStream().filter((final ItemStack itemStack) -> {
                    final Item item = itemStack.getItem();

                    return item instanceof BlockItem || item instanceof BucketItem;
                }).collect(Collectors.toList());

                itemList.addAll(items);
            }
        }

        this.scrollPosition = 0;
        this.handler.scrollItems(0);
    }

    private void searchForTags(final String string) {
        final int index = string.indexOf(58);
        final class_5414<Item> tagContainer = ItemTags.getContainer();
        final Predicate<Identifier> predicate = index == -1
                ? (identifier) -> identifier.getPath().contains(string)
                : (final Identifier identifier) -> identifier.getNamespace().contains(string.substring(0, index).trim()) && identifier.getPath().contains(string.substring(index + 1).trim());

        tagContainer.method_30211().stream().filter(predicate).forEach((final Identifier identifier) -> this.searchResultTags.put(identifier, tagContainer.method_30210(identifier)));
    }

    private void addCustomItems(final String search) {
        final DefaultedList<ItemStack> itemList = this.handler.itemList;

        for (final Item item : XRadiationItems.ITEMS) {
            if (search == null || item.getName().getString().toLowerCase().contains(search)) {
                itemList.add(item.getStackForRender());
            }
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            final double x = mouseX - this.x;
            final double y = mouseY - this.y;

            for (final ItemGroup itemGroup : GROUPS) {
                if (this.isClickInTab(itemGroup, x, y)) {
                    return true;
                }
            }

            if (this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            final double x = mouseX - this.x;
            final double y = mouseY - this.y;

            this.scrolling = false;

            for (final ItemGroup itemGroup : GROUPS) {
                if (this.isClickInTab(itemGroup, x, y)) {
                    this.setSelectedTab(itemGroup);

                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void onMouseClick(@Nullable final Slot slot, final int invSlot, final int clickData, final SlotActionType actionType) {
        if (this.isValidSlot(slot)) {
            this.searchBox.setCursorToEnd();
            this.searchBox.setSelectionEnd(0);

            Configuration.INSTANCE.toggleItems(slot.getStack());
            Configuration.INSTANCE.write();

            this.updateButtons();
        }
    }

    private void updateButtons() {
        this.applyButton.active = Configuration.INSTANCE.canApply();
        this.discardButton.active = !Configuration.INSTANCE.isSaved();
        this.resetButton.active = !Configuration.INSTANCE.isDefault();
    }

    private boolean hasScrollbar() {
        return GROUPS.get(selectedTab).hasScrollbar() && this.handler.shouldShowScrollbar();
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        if (this.hasScrollbar()) {
            final int i = (this.handler.itemList.size() + 8) / 9 - 5;

            this.scrollPosition = (float) (this.scrollPosition - amount / i);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0, 1);
            this.handler.scrollItems(this.scrollPosition);

            return true;
        }

        return false;
    }

    @Override
    protected boolean isClickOutsideBounds(final double mouseX, final double mouseY, final int left, final int top, final int button) {
        return mouseX < left || mouseY < top || mouseX >= left + this.backgroundWidth || mouseY >= top + this.backgroundHeight
                && !this.isClickInTab(GROUPS.get(selectedTab), mouseX, mouseY);
    }

    protected boolean isClickInScrollbar(final double mouseX, final double mouseY) {
        final int k = this.x + 175;
        final int l = this.y + 18;

        return mouseX >= k && mouseY >= l && mouseX < k + 14 && mouseY < l + 112;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.scrolling) {
            this.scrollPosition = (float) (mouseY - this.y - 25.5F) / 97;
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0, 1);
            this.handler.scrollItems(this.scrollPosition);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public final void render(final MatrixStack matrices, final int mouseX, final int mouseY, final float delta) {
        this.renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);

        for (final ItemGroup itemGroup : GROUPS) {
            if (this.renderTabTooltipIfHovered(matrices, itemGroup, mouseX, mouseY)) {
                break;
            }
        }

        color4f(1, 1, 1, 1);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (selectedTab == GROUPS.indexOf(ItemGroup.SEARCH)) {
            final List<Text> result = new ArrayList<>(stack.getTooltip(this.player, client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL));
            final Item item = stack.getItem();
            final ItemGroup itemGroup = item.getGroup();

            this.searchResultTags.forEach((identifier, tag) -> {
                if (tag.contains(item)) {
                    result.add(1, (new LiteralText("#" + identifier)).formatted(Formatting.DARK_PURPLE));
                }
            });

            if (itemGroup != null) {
                result.add(1, (new TranslatableText(itemGroup.getTranslationKey())).formatted(Formatting.BLUE));
            }

            this.renderTooltip(matrices, result, x, y);
        } else {
            super.renderTooltip(matrices, stack, x, y);
        }

    }

    @Override
    protected void drawForeground(final MatrixStack matrices, final int mouseX, final int mouseY) {
        final ItemGroup itemGroup = GROUPS.get(selectedTab);

        if (itemGroup.hasTooltip()) {
            RenderSystem.disableBlend();

            this.textRenderer.draw(matrices, I18n.translate(itemGroup.getTranslationKey()), 8, 6, 4210752);
        }
    }

    @Override
    protected void drawBackground(final MatrixStack matrices, final float delta, final int mouseX, final int mouseY) {
        color4f(1, 1, 1, 1);

        final ItemGroup itemGroup = GROUPS.get(selectedTab);

        int y = GROUPS.size();

        for (int k = 0; k < y; ++k) {
            final ItemGroup itemGroup2 = GROUPS.get(k);

            client.getTextureManager().bindTexture(TEXTURE);

            if (GROUPS.indexOf(itemGroup2) != selectedTab) {
                this.renderTabIcon(matrices, itemGroup2);
            }
        }

        client.getTextureManager().bindTexture(new Identifier("textures/gui/container/creative_inventory/tab_" + itemGroup.getTexture()));
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        for (int i = 0, max = 162 / 5 + 1; i < max; i++) {
            this.drawTexture(matrices, this.x + 8 + i * 5, this.y + 111, 3, 111, 5, 18);
        }

        this.searchBox.render(matrices, mouseX, mouseY, delta);

        y = this.y + 18;

        client.getTextureManager().bindTexture(TEXTURE);
        color4f(1, 1, 1, 1);

        if (itemGroup.hasScrollbar()) {
            this.drawTexture(matrices, this.x + 175, y + (int) (95 * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
        }

        this.renderTabIcon(matrices, itemGroup);
    }

    @Override
    protected void drawSlot(final MatrixStack matrices, final Slot slot) {
        super.drawSlot(matrices, slot);

        final Item item = slot.getStack().getItem();

        if (Configuration.INSTANCE.shouldAdd(item)) {
            fill(slot, 0x88A0FFA0);
        } else if (Configuration.INSTANCE.shouldRemove(item)) {
            fill(slot, 0x88FF4400);
        } else if (!Configuration.INSTANCE.isAllowed(item)) {
            fill(slot, 0x88000000);
        }
    }

    private boolean isClickInTab(final ItemGroup itemGroup, final double mouseX, final double mouseY) {
        final int column = itemGroup.getColumn();
        final int l;

        if (itemGroup.isSpecial()) {
            l = this.backgroundWidth - 28 * (6 - column) + 2;
        } else if (column > 0) {
            l = 28 * column + column;
        } else {
            l = 0;
        }

        final int m = itemGroup.isTopRow() ? -32 : this.backgroundHeight;

        return mouseX >= l && mouseX <= l + 28 && mouseY >= m && mouseY <= m + 32;
    }

    private boolean renderTabTooltipIfHovered(final MatrixStack matrixStack, final ItemGroup itemGroup, final int x, final int y) {
        final int column = itemGroup.getColumn();
        final int l;

        if (itemGroup.isSpecial()) {
            l = this.backgroundWidth - 28 * (6 - column) + 2;
        } else if (column > 0) {
            l = 28 * column + column;
        } else {
            l = 0;
        }

        final int m = itemGroup.isTopRow() ? -32 : this.backgroundHeight;

        if (this.isPointWithinBounds(l + 3, m + 3, 23, 27, x, y)) {
            this.renderTooltip(matrixStack, new TranslatableText(itemGroup.getTranslationKey()), x, y);

            return true;
        }

        return false;
    }

    private void renderTabIcon(MatrixStack matrixStack, ItemGroup itemGroup) {
        final boolean isTop = itemGroup.isTopRow();
        final int column = itemGroup.getColumn();
        final int j = column * 28;
        int k = 0;
        int l = this.x + 28 * column;
        int m = this.y;

        if (GROUPS.indexOf(itemGroup) == selectedTab) {
            k += 32;
        }

        if (itemGroup.isSpecial()) {
            l = this.x + this.backgroundWidth - 28 * (6 - column);
        } else if (column > 0) {
            l += column;
        }

        if (isTop) {
            m -= 28;
        } else {
            k += 64;
            m += this.backgroundHeight - 4;
        }

        this.drawTexture(matrixStack, l, m, j, k, 28, 32);
        this.itemRenderer.zOffset = 100.0F;
        l += 6;
        m += 8 + (isTop ? 1 : -1);
        RenderSystem.enableRescaleNormal();
        ItemStack itemStack = itemGroup.getIcon();
        this.itemRenderer.renderInGuiWithOverrides(itemStack, l, m);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, l, m);
        this.itemRenderer.zOffset = 0.0F;
    }

    private void setSelectedTab(ItemGroup group) {
        final int lastTabIndex = selectedTab;

        selectedTab = GROUPS.indexOf(group);

        this.cursorDragSlots.clear();
        this.handler.itemList.clear();

        if (group != ItemGroup.SEARCH) {
            group.appendStacks(this.handler.itemList);


            this.handler.itemList.removeIf((final ItemStack itemStack) -> !Configuration.isAcceptable(itemStack));

            if (group == ItemGroup.DECORATIONS) {
                this.addCustomItems(null);
            }
        }

        if (this.searchBox != null) {
            if (group == ItemGroup.SEARCH) {
                this.searchBox.setVisible(true);
                this.searchBox.setFocusUnlocked(false);
                this.searchBox.setSelected(true);
                if (lastTabIndex != GROUPS.indexOf(group)) {
                    this.searchBox.setText("");
                }

                this.search();
            } else {
                this.searchBox.setVisible(false);
                this.searchBox.setFocusUnlocked(true);
                this.searchBox.setSelected(false);
                this.searchBox.setText("");
            }
        }

        this.scrollPosition = 0.0F;
        this.handler.scrollItems(0.0F);
    }

    private static void color4f(final float r, final float g, final float b, final float a) {
        //noinspection deprecation
        RenderSystem.color4f(r, g, b, a);
    }

    @Environment(EnvType.CLIENT)
    private static class LockedSlot extends Slot {
        public LockedSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class XrayScreenHandler extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();

        public XrayScreenHandler() {
            super(null, 0);

            for (int k = 0; k < 5; ++k) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new LockedSlot(XrayScreen.inventory, k * 9 + j, 9 + j * 18, 18 + k * 18));
                }
            }

            this.scrollItems(0.0F);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        public void scrollItems(float position) {
            int i = (this.itemList.size() + 9 - 1) / 9 - 5;
            int j = (int) ((double) (position * (float) i) + 0.5D);
            if (j < 0) {
                j = 0;
            }

            for (int k = 0; k < 5; ++k) {
                for (int l = 0; l < 9; ++l) {
                    int m = l + (k + j) * 9;
                    if (m >= 0 && m < this.itemList.size()) {
                        XrayScreen.inventory.setStack(l + k * 9, this.itemList.get(m));
                    } else {
                        XrayScreen.inventory.setStack(l + k * 9, ItemStack.EMPTY);
                    }
                }
            }

        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        @Override
        public ItemStack transferSlot(PlayerEntity player, int index) {
            if (index >= this.slots.size() - 9 && index < this.slots.size()) {
                Slot slot = this.slots.get(index);
                if (slot != null && slot.hasStack()) {
                    slot.setStack(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
            return slot.inventory != XrayScreen.inventory;
        }

        @Override
        public boolean canInsertIntoSlot(Slot slot) {
            return slot.inventory != XrayScreen.inventory;
        }
    }
}
