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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.texture.TextureManager;
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
import net.minecraft.tag.TagGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import user11681.xradiation.Configuration;
import user11681.xradiation.XRadiation;
import user11681.xradiation.XRadiationItems;
import user11681.xradiation.gui.widget.CounterclockwiseArrowButton;
import user11681.xradiation.gui.widget.CrossButton;
import user11681.xradiation.gui.widget.SquareTexturedButton;
import user11681.xradiation.gui.widget.TickButton;

@SuppressWarnings("SameParameterValue")
public class XrayScreen extends AbstractInventoryScreen<XrayScreen.XrayScreenHandler> {
    public static final Identifier texture = new Identifier("textures/gui/container/creative_inventory/tabs.png");

    private static final SimpleInventory inventory = new SimpleInventory(45);
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final TextureManager textureManager = client.getTextureManager();
    private static final List<ItemGroup> groups = new ArrayList<>(Arrays.asList(ItemGroup.GROUPS));

    private static final String enabledKey = String.format("%s:enabled", XRadiation.ID);
    private static final String disabledKey = String.format("%s:disabled", XRadiation.ID);
    private static final String allowedKey = String.format("%s:allowed", XRadiation.ID);
    private static final String disallowedKey = String.format("%s:disallowed", XRadiation.ID);

    private static int selectedTab = groups.indexOf(ItemGroup.BUILDING_BLOCKS);

    static {
        groups.remove(ItemGroup.INVENTORY);
        groups.remove(ItemGroup.HOTBAR);
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
        super(new XrayScreenHandler(), XRadiation.getPlayer().inventory, LiteralText.EMPTY);

        this.player = XRadiation.getPlayer();
        this.player.currentScreenHandler = this.handler;
        this.searchResultTags = new TreeMap<>();
        this.passEvents = true;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
    }

    private static void fill(Slot slot, int color) {
        fill(new MatrixStack(), slot.x, slot.y, slot.x + 16, slot.y + 16, color);
    }

    @Override
    protected void init() {
        super.init();

        client.keyboard.setRepeatEvents(true);

        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setHasBorder(false);
        this.searchBox.setVisible(false);
        this.searchBox.setEditableColor(0x888888);
        this.children.add(this.searchBox);

        this.applyButton = this.addButton(new TickButton(this.x + 8, this.y + 110, (ButtonWidget button) -> {
            Configuration.instance.apply();

            this.updateButtons();
        }));

        this.discardButton = this.addButton(new CrossButton(this.x + 32, this.y + 110, (ButtonWidget button) -> {
            Configuration.instance.discard();

            this.updateButtons();
        }));

        this.resetButton = this.addButton(new CounterclockwiseArrowButton(this.x + 56, this.y + 110, (ButtonWidget button) -> {
            Configuration.instance.reset();

            this.updateButtons();
        }));

        this.updateButtons();

        this.setSelectedTab(groups.get(selectedTab));
//        selectedTab = -1;

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
        int x = this.x;

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

        client.keyboard.setRepeatEvents(false);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (!this.ignoreTypedCharacter && selectedTab == groups.indexOf(ItemGroup.SEARCH)) {
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

        if (selectedTab != groups.indexOf(ItemGroup.SEARCH)) {
            if (client.options.keyChat.matchesKey(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(ItemGroup.SEARCH);

                return true;
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        boolean bl = !this.isValidSlot(this.focusedSlot) || this.focusedSlot.hasStack();
        boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).method_30103().isPresent();

        if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;

            return true;
        }

        String string = this.searchBox.getText();

        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                this.search();
            }

            return true;
        }

        return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        DefaultedList<ItemStack> itemList = this.handler.itemList;

        itemList.clear();
        this.searchResultTags.clear();
        String string = this.searchBox.getText();

        if (string.isEmpty()) {
            for (Item item : Item.BLOCK_ITEMS.values()) {
                item.appendStacks(ItemGroup.SEARCH, itemList);
            }
        } else {
            final SearchableContainer<ItemStack> container;

            this.addCustomItems(string);

            if (allowedKey.startsWith(string) || enabledKey.startsWith(string)) {
                itemList.addAll(Configuration.instance.allowedBlocks.parallelStream().map((Block block) -> block.asItem().getDefaultStack()).collect(Collectors.toList()));
                itemList.addAll(Configuration.instance.allowedFluids.parallelStream().map((Fluid fluid) -> fluid.getBucketItem().getDefaultStack()).collect(Collectors.toList()));
            } else if (disallowedKey.startsWith(string) || disabledKey.startsWith(string)) {
                for (Item item : Item.BLOCK_ITEMS.values()) {
                    item.appendStacks(ItemGroup.SEARCH, itemList);
                }

                itemList.removeIf(Configuration.instance::isAllowed);
            } else {
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    container = client.getSearchableContainer(SearchManager.ITEM_TAG);

                    this.searchForTags(string);
                } else {
                    container = client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
                }

                final List<ItemStack> items = container.findAll(string.toLowerCase(Locale.ROOT)).parallelStream().filter((ItemStack itemStack) -> {
                    Item item = itemStack.getItem();

                    return item instanceof BlockItem || item instanceof BucketItem;
                }).collect(Collectors.toList());

                itemList.addAll(items);
            }
        }

        this.scrollPosition = 0;
        this.handler.scrollItems(0);
    }

    private void searchForTags(String string) {
        int index = string.indexOf(58);
        TagGroup<Item> tagContainer = ItemTags.getTagGroup();

        Predicate<Identifier> predicate = index == -1
            ? (identifier) -> identifier.getPath().contains(string)
            : (Identifier identifier) -> identifier.getNamespace().contains(string.substring(0, index).trim()) && identifier.getPath().contains(string.substring(index + 1).trim());

        tagContainer.getTagIds().stream().filter(predicate).forEach((Identifier identifier) -> this.searchResultTags.put(identifier, tagContainer.getTag(identifier)));
    }

    private void addCustomItems(String search) {
        DefaultedList<ItemStack> itemList = this.handler.itemList;

        for (Item item : XRadiationItems.ITEMS) {
            if (search == null || item.getName().getString().toLowerCase().contains(search)) {
                itemList.add(item.getDefaultStack());
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double x = mouseX - this.x;
            double y = mouseY - this.y;

            for (ItemGroup itemGroup : groups) {
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double x = mouseX - this.x;
            double y = mouseY - this.y;

            this.scrolling = false;

            for (ItemGroup itemGroup : groups) {
                if (this.isClickInTab(itemGroup, x, y)) {
                    this.setSelectedTab(itemGroup);

                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void onMouseClick(@Nullable Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (this.isValidSlot(slot)) {
            this.searchBox.setCursorToEnd();
            this.searchBox.setSelectionEnd(0);

            Configuration.instance.toggleItems(slot.getStack());
            Configuration.instance.write();

            this.updateButtons();
        }
    }

    private void updateButtons() {
        this.applyButton.active = Configuration.instance.canApply();
        this.discardButton.active = !Configuration.instance.isSaved();
        this.resetButton.active = !Configuration.instance.isDefault();
    }

    private boolean hasScrollbar() {
        return groups.get(selectedTab).hasScrollbar() && this.handler.shouldShowScrollbar();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
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
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return mouseX < left || mouseY < top || mouseX >= left + this.backgroundWidth || mouseY >= top + this.backgroundHeight
            && !this.isClickInTab(groups.get(selectedTab), mouseX, mouseY);
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int k = this.x + 175;
        int l = this.y + 18;

        return mouseX >= k && mouseY >= l && mouseX < k + 14 && mouseY < l + 112;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            this.scrollPosition = (float) (mouseY - this.y - 25.5F) / 97;
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0, 1);
            this.handler.scrollItems(this.scrollPosition);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);

        for (ItemGroup itemGroup : groups) {
            if (this.renderTabTooltipIfHovered(matrices, itemGroup, mouseX, mouseY)) {
                break;
            }
        }

        resetColor();

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (selectedTab == groups.indexOf(ItemGroup.SEARCH)) {
            List<Text> result = new ArrayList<>(stack.getTooltip(this.player, client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL));
            Item item = stack.getItem();
            ItemGroup itemGroup = item.getGroup();

            this.searchResultTags.forEach((identifier, tag) -> {
                if (tag.contains(item)) {
                    result.add(1, (new LiteralText("#" + identifier)).formatted(Formatting.DARK_PURPLE));
                }
            });

            if (itemGroup != null) {
                result.add(1, new TranslatableText(itemGroup.getTranslationKey().asString()).formatted(Formatting.BLUE));
            }

            this.renderTooltip(matrices, result, x, y);
        } else {
            super.renderTooltip(matrices, stack, x, y);
        }

    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        ItemGroup itemGroup = groups.get(selectedTab);

        if (itemGroup.shouldRenderName()) {
            RenderSystem.disableBlend();

            this.textRenderer.draw(matrices, itemGroup.getTranslationKey(), 8, 6, 4210752);
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        resetColor();

        ItemGroup itemGroup = groups.get(selectedTab);

        int y = groups.size();

        for (int k = 0; k < y; ++k) {
            ItemGroup itemGroup2 = groups.get(k);

            textureManager.bindTexture(texture);

            if (groups.indexOf(itemGroup2) != selectedTab) {
                this.renderTabIcon(matrices, itemGroup2);
            }
        }

        textureManager.bindTexture(new Identifier("textures/gui/container/creative_inventory/tab_" + itemGroup.getTexture()));
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        for (int i = 0, max = 162 / 5 + 1; i < max; i++) {
            this.drawTexture(matrices, this.x + 8 + i * 5, this.y + 111, 3, 111, 5, 18);
        }

        this.searchBox.render(matrices, mouseX, mouseY, delta);

        y = this.y + 18;

        textureManager.bindTexture(texture);

        resetColor();

        if (itemGroup.hasScrollbar()) {
            this.drawTexture(matrices, this.x + 175, y + (int) (95 * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
        }

        this.renderTabIcon(matrices, itemGroup);
    }

    @Override
    protected void drawSlot(MatrixStack matrices, Slot slot) {
        super.drawSlot(matrices, slot);

        Item item = slot.getStack().getItem();

        if (Configuration.instance.shouldAdd(item)) {
            fill(slot, 0x88A0FFA0);
        } else if (Configuration.instance.shouldRemove(item)) {
            fill(slot, 0x88FF4400);
        } else if (!Configuration.instance.isAllowed(item)) {
            fill(slot, 0x88000000);
        }
    }

    private boolean isClickInTab(ItemGroup itemGroup, double mouseX, double mouseY) {
        int column = itemGroup.getColumn();
        int l;

        if (itemGroup.isSpecial()) {
            l = this.backgroundWidth - 28 * (6 - column) + 2;
        } else if (column > 0) {
            l = 28 * column + column;
        } else {
            l = 0;
        }

        int m = itemGroup.isTopRow() ? -32 : this.backgroundHeight;

        return mouseX >= l && mouseX <= l + 28 && mouseY >= m && mouseY <= m + 32;
    }

    private boolean renderTabTooltipIfHovered(MatrixStack matrixStack, ItemGroup itemGroup, int x, int y) {
        int column = itemGroup.getColumn();
        int l;

        if (itemGroup.isSpecial()) {
            l = this.backgroundWidth - 28 * (6 - column) + 2;
        } else if (column > 0) {
            l = 28 * column + column;
        } else {
            l = 0;
        }

        int m = itemGroup.isTopRow() ? -32 : this.backgroundHeight;

        if (this.isPointWithinBounds(l + 3, m + 3, 23, 27, x, y)) {
            this.renderTooltip(matrixStack, itemGroup.getTranslationKey(), x, y);

            return true;
        }

        return false;
    }

    private void renderTabIcon(MatrixStack matrixStack, ItemGroup itemGroup) {
        boolean isTop = itemGroup.isTopRow();
        int column = itemGroup.getColumn();
        int j = column * 28;
        int k = 0;
        int l = this.x + 28 * column;
        int m = this.y;

        if (groups.indexOf(itemGroup) == selectedTab) {
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
        int lastTabIndex = selectedTab;

        selectedTab = groups.indexOf(group);

        this.cursorDragSlots.clear();
        this.handler.itemList.clear();

        if (group != ItemGroup.SEARCH) {
            group.appendStacks(this.handler.itemList);

            this.handler.itemList.removeIf((ItemStack itemStack) -> !Configuration.isAcceptable(itemStack));

            if (group == ItemGroup.DECORATIONS) {
                this.addCustomItems(null);
            }
        }

        if (this.searchBox != null) {
            if (group == ItemGroup.SEARCH) {
                this.searchBox.setVisible(true);
                this.searchBox.setFocusUnlocked(false);
                this.searchBox.setSelected(true);
                if (lastTabIndex != groups.indexOf(group)) {
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

    private static void resetColor() {
        //noinspection deprecation
        RenderSystem.color4f(1, 1, 1, 1);
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
