package user11681.xradiation.mixin.mixin.item;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import user11681.xradiation.registry.RegistryData;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "appendBlocks", at = @At("HEAD"))
    private void mapItemToBlocks(final Map<Block, Item> map, final Item item, final CallbackInfo info) {
        ReferenceOpenHashSet<Block> mappedBlocks = RegistryData.ITEM_BLOCKS.get(item);

        if (mappedBlocks == null) {
            RegistryData.ITEM_BLOCKS.put(item, mappedBlocks = new ReferenceOpenHashSet<>());
        }

        mappedBlocks.add(this.getBlock());

        for (final Map.Entry<Block, Item> entry : map.entrySet()) {
            if (entry.getValue() == item) {
                mappedBlocks.add(entry.getKey());
            }
        }
    }
}
