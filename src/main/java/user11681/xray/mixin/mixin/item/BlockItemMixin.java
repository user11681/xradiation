package user11681.xray.mixin.mixin.item;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import user11681.xray.registry.BlockItemData;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "appendBlocks", at = @At("HEAD"))
    private void mapItemToBlocks(final Map<Block, Item> map, final Item item, final CallbackInfo info) {
        final Set<Block> blocks = new ObjectOpenHashSet<>();
        final Block block = this.getBlock();

        blocks.add(block);

        for (final Map.Entry<Block, Item> entry : map.entrySet()) {
            final Item value = entry.getValue();

            if (value == item) {
                blocks.add(entry.getKey());

                BlockItemData.ITEM_BLOCKS.get(value).add(block);
            }
        }

        BlockItemData.ITEM_BLOCKS.computeIfAbsent(item, (final Item key) -> new ObjectOpenHashSet<>()).addAll(blocks);
    }
}
