package fhannenheim.betterpickblock.mixin;

import fhannenheim.betterpickblock.BetterPickBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.security.util.Resources_zh_CN;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientClass {
    @Shadow @Nullable public HitResult crosshairTarget;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Nullable public ClientWorld world;

    @Shadow protected abstract ItemStack addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity);

    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Inject(method = "doItemPick",at = @At(value = "HEAD"), cancellable = true)
    private void injected(CallbackInfo ci){
        if (crosshairTarget != null && this.crosshairTarget.getType() != HitResult.Type.MISS) {
            boolean bl = player.abilities.creativeMode;
            BlockEntity blockEntity = null;
            HitResult.Type type = this.crosshairTarget.getType();
            ItemStack itemStack12;
            if (type == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult)this.crosshairTarget).getBlockPos();
                BlockState blockState = this.world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (blockState.isAir()) {
                    return;
                }

                itemStack12 = block.getPickStack(this.world, blockPos, blockState);
                if (itemStack12.isEmpty()) {
                    return;
                }

                if (bl && Screen.hasControlDown() && block.hasBlockEntity()) {
                    blockEntity = this.world.getBlockEntity(blockPos);
                }
            } else {
                if (type != HitResult.Type.ENTITY || !bl) {
                    return;
                }

                Entity entity = ((EntityHitResult)this.crosshairTarget).getEntity();
                if (entity instanceof PaintingEntity) {
                    itemStack12 = new ItemStack(Items.PAINTING);
                } else if (entity instanceof LeashKnotEntity) {
                    itemStack12 = new ItemStack(Items.LEAD);
                } else if (entity instanceof ItemFrameEntity) {
                    ItemFrameEntity itemFrameEntity = (ItemFrameEntity)entity;
                    ItemStack itemStack4 = itemFrameEntity.getHeldItemStack();
                    if (itemStack4.isEmpty()) {
                        itemStack12 = new ItemStack(Items.ITEM_FRAME);
                    } else {
                        itemStack12 = itemStack4.copy();
                    }
                } else if (entity instanceof AbstractMinecartEntity) {
                    AbstractMinecartEntity abstractMinecartEntity = (AbstractMinecartEntity)entity;
                    Item item6;
                    switch(abstractMinecartEntity.getMinecartType()) {
                        case FURNACE:
                            item6 = Items.FURNACE_MINECART;
                            break;
                        case CHEST:
                            item6 = Items.CHEST_MINECART;
                            break;
                        case TNT:
                            item6 = Items.TNT_MINECART;
                            break;
                        case HOPPER:
                            item6 = Items.HOPPER_MINECART;
                            break;
                        case COMMAND_BLOCK:
                            item6 = Items.COMMAND_BLOCK_MINECART;
                            break;
                        default:
                            item6 = Items.MINECART;
                    }

                    itemStack12 = new ItemStack(item6);
                } else if (entity instanceof BoatEntity) {
                    itemStack12 = new ItemStack(((BoatEntity)entity).asItem());
                } else if (entity instanceof ArmorStandEntity) {
                    itemStack12 = new ItemStack(Items.ARMOR_STAND);
                } else if (entity instanceof EndCrystalEntity) {
                    itemStack12 = new ItemStack(Items.END_CRYSTAL);
                } else {
                    SpawnEggItem spawnEggItem = SpawnEggItem.forEntity(entity.getType());
                    if (spawnEggItem == null) {
                        return;
                    }

                    itemStack12 = new ItemStack(spawnEggItem);
                }
            }

            if (itemStack12.isEmpty()) {
                String string = "";
                if (type == HitResult.Type.BLOCK) {
                    string = Registry.BLOCK.getId(this.world.getBlockState(((BlockHitResult)this.crosshairTarget).getBlockPos()).getBlock()).toString();
                } else if (type == HitResult.Type.ENTITY) {
                    string = Registry.ENTITY_TYPE.getId(((EntityHitResult)this.crosshairTarget).getEntity().getType()).toString();
                }

                LOGGER.warn((String)"Picking on: [{}] {} gave null item", (Object)type, (Object)string);
            } else {
                PlayerInventory playerInventory = this.player.inventory;
                if (blockEntity != null) {
                    this.addBlockEntityNbt(itemStack12, blockEntity);
                }

                int i = playerInventory.getSlotWithStack(itemStack12);
                if (bl) {
                    playerInventory.addPickBlock(itemStack12);
                    this.interactionManager.clickCreativeStack(this.player.getStackInHand(Hand.MAIN_HAND), 36 + playerInventory.selectedSlot);
                    return;
                }
                if (i == -1){
                    String itemID = Registry.ITEM.getId(itemStack12.getItem()).toString();
                    if(BetterPickBlock.itemAlternatives.keySet().contains(itemID)){
                        String[] alternativeIDs = BetterPickBlock.itemAlternatives.get(itemID);
                        for (String alternative : alternativeIDs){
                            Identifier id = Identifier.tryParse(alternative);
                            itemStack12 = Registry.ITEM.get(id).getDefaultStack();
                            i = playerInventory.getSlotWithStack(itemStack12);
                            if (i != -1)
                                break;
                        }
                    }
                }
                if (i != -1) {
                    if (PlayerInventory.isValidHotbarIndex(i)) {
                        playerInventory.selectedSlot = i;
                    } else {
                        this.interactionManager.pickFromInventory(i);
                    }
                }

            }
        }
        ci.cancel();
    }
}
