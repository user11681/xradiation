package user11681.xradiation.mixin.mixin.item;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.WallStandingBlockItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import user11681.xradiation.registry.RegistryData;

@Mixin(WallStandingBlockItem.class)
abstract class WallStandingBlockItemMixin {
    @Shadow
    @Final
    protected Block wallBlock;

    @Inject(method = "appendBlocks", at = @At("HEAD"))
    private void mapItemToBlocks(Map<Block, Item> map, Item item, CallbackInfo info) {
        ReferenceOpenHashSet<Block> mappedBlocks = RegistryData.itemBlocks.get(item);

        if (mappedBlocks == null) {
            RegistryData.itemBlocks.put(item, mappedBlocks = new ReferenceOpenHashSet<>());
        }

        mappedBlocks.add(this.wallBlock);

        for (Map.Entry<Block, Item> entry : map.entrySet()) {
            if (entry.getValue() == item) {
                mappedBlocks.add(entry.getKey());
            }
        }
    }
}
