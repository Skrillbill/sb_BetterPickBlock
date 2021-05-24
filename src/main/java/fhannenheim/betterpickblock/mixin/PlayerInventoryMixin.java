package fhannenheim.betterpickblock.mixin;

import com.sun.org.apache.bcel.internal.generic.RET;
import fhannenheim.betterpickblock.BetterPickBlock;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow @Final public DefaultedList<ItemStack> main;

    @Shadow public int selectedSlot;

    @Inject(method = "getSwappableHotbarSlot",at = @At(value = "HEAD"), cancellable = true)
    private void injected(CallbackInfoReturnable<Integer> ci){
        int hotbarSlot = 0;

        int i;
        int l;
        for(i = 0; i < 9; ++i) {
            l = (this.selectedSlot + i) % 9;
            if (((ItemStack)this.main.get(l)).isEmpty()) {
                ci.setReturnValue(l);
                return;
            }
        }

        outerLoop:
        for(i = 0; i < 9; ++i) {
            l = (this.selectedSlot + i) % 9;
            for (int j = 0; j < BetterPickBlock.dontReplaceItemTags.size(); j++){
                if(TagRegistry.item(Identifier.tryParse(BetterPickBlock.dontReplaceItemTags.get(j))).contains(((ItemStack)this.main.get(l)).getItem())){
                    continue outerLoop;
                }
            }
            if (!((ItemStack)this.main.get(l)).hasEnchantments() &&
                    !BetterPickBlock.dontReplaceItemIDs.contains(Registry.ITEM.getId(((ItemStack)this.main.get(l)).getItem()).toString())) {
                ci.setReturnValue(l);
                return;
            }
        }
        ci.setReturnValue(hotbarSlot);
    }
}
