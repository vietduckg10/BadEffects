package com.ducvn.badeffects.events;

import com.ducvn.badeffects.config.BadEffectsConfig;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class BadEffectsEvents {

    private static int onFireTick = 0;
    private static int aboveBuildLimitTick = 0;
    private static List<Item> hotItem = new ArrayList<>(
            Arrays.asList(
                    Items.MAGMA_BLOCK.getItem(), Items.TORCH.getItem(), Items.LANTERN.getItem(),
                    Items.JACK_O_LANTERN.getItem(), Items.LAVA_BUCKET.getItem()
            )
    );
    private static int onColdBiomeTick = 0;
    private static List<Block> hotBlock = new ArrayList<>(
            Arrays.asList(
                    Blocks.TORCH.getBlock(), Blocks.MAGMA_BLOCK.getBlock(), Blocks.LANTERN.getBlock(),
                    Blocks.JACK_O_LANTERN.getBlock(), Blocks.LAVA.getBlock()
            )
    );
    private static int lightningTick = 0;
    private static List<ResourceLocation> rawFood = new ArrayList<>(
            Arrays.asList(
                    ResourceLocation.tryParse("minecraft:potato"), ResourceLocation.tryParse("minecraft:carrot"),
                    ResourceLocation.tryParse("minecraft:apple"), ResourceLocation.tryParse("minecraft:porkchop"),
                    ResourceLocation.tryParse("minecraft:cod"), ResourceLocation.tryParse("minecraft:salmon"),
                    ResourceLocation.tryParse("minecraft:tropical_fish"), ResourceLocation.tryParse("minecraft:melon"),
                    ResourceLocation.tryParse("minecraft:beef"), ResourceLocation.tryParse("minecraft:chicken"),
                    ResourceLocation.tryParse("minecraft:rabbit"), ResourceLocation.tryParse("minecraft:mutton"),
                    ResourceLocation.tryParse("minecraft:beetroot"), ResourceLocation.tryParse("minecraft:sweet_berries"),
                    ResourceLocation.tryParse("minecraft:rotten_flesh")
            )
    );

    @SubscribeEvent
    public static void GiveHeatstrokeEvent(TickEvent.PlayerTickEvent event){
        World world = event.player.level;
        if (!world.isClientSide && world.getBiome(event.player.blockPosition()).getTemperature(event.player.blockPosition()) > 1.7F
        && !BadEffectsConfig.heat_stroke.get()){
            PlayerEntity player = event.player;
            if (player.isOnFire()){
                onFireTick++;
                if (onFireTick % 100 == 1 && onFireTick != 1){
                    player.displayClientMessage(new TranslationTextComponent("You got heatstroke").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.YELLOW),true);
                    player.addEffect(new EffectInstance(Effects.WEAKNESS, 100));
                    Random roll = new Random();
                    if (roll.nextBoolean()){
                        player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100));
                    }
                    else {
                        player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 100));
                    }
                }
            }
            else{
                onFireTick = 0;
            }
        }
    }

    @SubscribeEvent
    public static void GiveBrokenLegEvent(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.broke_leg.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (event.getSource() == DamageSource.FALL && event.getAmount() >= 2.0F){
                player.displayClientMessage(new TranslationTextComponent("You hurt your legs").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.RED),true);
                System.out.println(event.getAmount());
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 20 + ((int) event.getAmount() - 2) * 20));
                if (event.getAmount() >= 10){
                    player.displayClientMessage(new TranslationTextComponent("Your legs are broken").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.RED),true);
                    player.addEffect(new EffectInstance(Effects.WITHER, 20 + ((int) event.getAmount() - 6) * 20));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveLackOfOxygenEvent(TickEvent.PlayerTickEvent event){
        World world = event.player.level;
        if (!world.isClientSide && !BadEffectsConfig.lack_of_oxygen.get()){
            PlayerEntity player = event.player;
            if (player.getY() >= 256){
                aboveBuildLimitTick++;
                if (aboveBuildLimitTick > 100){
                    player.addEffect(new EffectInstance(Effects.CONFUSION));
                    if (aboveBuildLimitTick > 200){
                        player.displayClientMessage(new TranslationTextComponent("You need oxygen").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.WHITE),true);
                        player.addEffect(new EffectInstance(Effects.WITHER));
                    }
                }
            }
            else {
                aboveBuildLimitTick = 0;
            }
        }
    }

    @SubscribeEvent
    public static void GiveNauseaWhileDrowning(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.nausea_drowning.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (event.getSource() == DamageSource.DROWN){
                Random roll = new Random();
                if (roll.nextDouble() < 0.34D) {
                    player.displayClientMessage(new TranslationTextComponent("You're drowning").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.AQUA), true);
                    player.addEffect(new EffectInstance(Effects.CONFUSION, 200));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveFrostBiteEvent(TickEvent.PlayerTickEvent event){
        World world = event.player.level;
        if (!world.isClientSide && world.getBiome(event.player.blockPosition()).getTemperature(event.player.blockPosition()) < 0.6F
        && !BadEffectsConfig.frostbite.get()){
            PlayerEntity player = event.player;
            if (!hotItem.contains(player.getItemInHand(Hand.MAIN_HAND).getItem())
                    && !hotItem.contains(player.getItemInHand(Hand.OFF_HAND).getItem())
                    && !isHotSourceAround(event.player.blockPosition(), world)){
                onColdBiomeTick++;
                if (onColdBiomeTick >= 1200){
                    if (onColdBiomeTick == 1200) {
                        player.displayClientMessage(new TranslationTextComponent("You start to feel hungry").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_GREEN), true);
                    }
                    player.addEffect(new EffectInstance(Effects.HUNGER));
                    if ((onColdBiomeTick - 1700) % 100 == 0 && onColdBiomeTick > 1700) {
                        player.displayClientMessage(new TranslationTextComponent("You got frostbite").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.AQUA), true);
                        player.hurt(DamageSource.WITHER, 2.0F + (float) (onColdBiomeTick - 1800) / 600F);
                    }
                }
            }
            else {
                onColdBiomeTick = 0;
            }
        }
    }

    private static boolean isHotSourceAround(BlockPos pos, World world){
        int posX;
        int posY;
        int posZ;
        for (posX = (pos.getX() - 2); posX <= (pos.getX() + 2); posX++){
            for (posY = (pos.getY() - 2); posY <= (pos.getY() + 2); posY++){
                for (posZ = (pos.getZ() - 2); posZ <= (pos.getZ() + 2); posZ++){
                    BlockState state = world.getBlockState(new BlockPos(posX, posY, posZ));
                    if (hotBlock.contains(state.getBlock())){
                        return true;
                    }
                    if (state.getBlock() instanceof CampfireBlock
                    || state.getBlock() instanceof AbstractFurnaceBlock){
                        if (state.getValue(BlockStateProperties.LIT)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void GiveFoodPoisoningEvent(LivingEntityUseItemEvent event){
        World world = event.getEntity().level;
        if(!world.isClientSide && event.getEntity() instanceof PlayerEntity && event.getItem().isEdible()
        && !BadEffectsConfig.food_poisoning.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (player.getUseItemRemainingTicks() == 1){
                double triggerChance = 0.5D;
                if (rawFood.contains(event.getItem().getItem().getRegistryName())){
                    triggerChance = 1.0D;
                }
                Random roll = new Random();
                if (roll.nextDouble() < triggerChance){
                    player.displayClientMessage(new TranslationTextComponent("You just got food poisoning").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_GREEN), true);
                    player.addEffect(new EffectInstance(Effects.CONFUSION, 200));
                    player.addEffect(new EffectInstance(Effects.POISON, 300));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveStuckEvent(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.stuck.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (event.getSource() == DamageSource.IN_WALL){
                Random roll = new Random();
                if (roll.nextDouble() < 0.1D){
                    player.displayClientMessage(new TranslationTextComponent("You're stuck and hard to move").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GRAY), true);
                    player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 200));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveStunEvent(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.stun.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (event.getSource().isExplosion()){
                player.displayClientMessage(new TranslationTextComponent("You are stunned by the explosion").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_PURPLE), true);
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, (int) (event.getAmount() * 10), 5));
                if (event.getAmount() >= 10F){
                    player.addEffect(new EffectInstance(Effects.CONFUSION, 200));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveKarmaEvent(LivingDeathEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getSource().getEntity() instanceof PlayerEntity
        && (event.getEntity() instanceof VillagerEntity || event.getEntity() instanceof IronGolemEntity)
        && !BadEffectsConfig.karma.get()){
            PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
            Random roll = new Random();
            if (roll.nextBoolean()){
                player.displayClientMessage(new TranslationTextComponent("You will pay for what you did").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_RED), true);
                player.addEffect(new EffectInstance(Effects.UNLUCK, 36000));
            }
        }
    }

    @SubscribeEvent
    public static void GiveLightningStrikeEvent(TickEvent.PlayerTickEvent event){
        World world = event.player.level;
        if (!world.isClientSide && !BadEffectsConfig.karma.get()){
            PlayerEntity player = event.player;
            if (player.getEffect(Effects.UNLUCK) != null){
                lightningTick++;
                if (lightningTick % 100 == 0){
                    Random roll = new Random();
                    if (roll.nextDouble() < 0.1D){
                        player.displayClientMessage(new TranslationTextComponent("This is your karma").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_PURPLE), true);
                        LightningBoltEntity lightningBoltEntity = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, world);
                        lightningBoltEntity.setPos(player.getX(), player.getY(), player.getZ());
                        world.addFreshEntity(lightningBoltEntity);
                    }
                }
            }
            else {
                lightningTick = 0;
            }
        }
    }

    @SubscribeEvent
    public static void GiveVisionDisruptEvent(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.vision_disrupt.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (event.getSource().isMagic() && player.getEffect(Effects.POISON) == null){
                Random roll = new Random();
                if (roll.nextDouble() < 0.34D){
                    player.displayClientMessage(new TranslationTextComponent("Your vision got disrupted by magic").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_PURPLE), true);
                    player.addEffect(new EffectInstance(Effects.BLINDNESS, 25, 4));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveBrokenHandEvent(BlockEvent.BreakEvent event){
        World world = event.getPlayer().level;
        if (!world.isClientSide && event.getPlayer() instanceof PlayerEntity
        && !BadEffectsConfig.broken_hand.get()){
            Block block = world.getBlockState(event.getPos()).getBlock();
            System.out.println(block.getTags());
            if (block.getTags().contains(new ResourceLocation("minecraft:logs"))
            || block.getTags().contains(new ResourceLocation("forge:logs"))
            || block.getTags().contains(new ResourceLocation("minecraft:glass"))
                    || block.getTags().contains(new ResourceLocation("forge:glass"))
                    || block.getTags().contains(new ResourceLocation("minecraft:glass_panes"))
                    || block.getTags().contains(new ResourceLocation("forge:glass_panes"))){
                PlayerEntity player = event.getPlayer();
                if (player.getItemInHand(Hand.MAIN_HAND).getItem() == Items.AIR){
                    Random roll = new Random();
                    if (roll.nextDouble() < 0.34D){
                        if (block.getTags().contains(new ResourceLocation("minecraft:glass"))
                                || block.getTags().contains(new ResourceLocation("forge:glass"))
                                || block.getTags().contains(new ResourceLocation("minecraft:glass_panes"))
                                || block.getTags().contains(new ResourceLocation("forge:glass_panes"))){
                            player.addEffect(new EffectInstance(Effects.WITHER, 100));
                        }
                        player.displayClientMessage(new TranslationTextComponent("You hurt your hand").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.RED), true);
                        player.hurt(DamageSource.WITHER, 2);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveInfectionEvent(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.infection.get()){
            if (event.getSource() == DamageSource.CACTUS
            || event.getSource() == DamageSource.SWEET_BERRY_BUSH){
                PlayerEntity player = (PlayerEntity) event.getEntity();
                if (player.getArmorCoverPercentage() < 1.0f){
                    double adjustment = 0;
                    if (event.getSource() == DamageSource.SWEET_BERRY_BUSH){
                        if (player.hasItemInSlot(EquipmentSlotType.FEET)){
                            adjustment = adjustment + 0.5D;
                        }
                        if (player.hasItemInSlot(EquipmentSlotType.LEGS)){
                            adjustment = adjustment + 0.5D;
                        }
                    }
                    if (event.getSource() == DamageSource.CACTUS){
                        adjustment = player.getArmorCoverPercentage();
                    }
                    Random roll = new Random();
                    if (roll.nextDouble() < (1D - 0.6D - 0.4D * adjustment)){
                        player.displayClientMessage(new TranslationTextComponent("You should wear more armor").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.DARK_GREEN), true);
                        player.addEffect(new EffectInstance(Effects.POISON, 200));
                    }
                }
            }
        }
    }
    //new update
    @SubscribeEvent
    public static void GiveHeadBumpEvent(LivingDamageEvent event){
        World world = event.getEntity().level;
        if (!world.isClientSide && event.getEntity() instanceof PlayerEntity
        && !BadEffectsConfig.head_bump.get()){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (event.getSource() == DamageSource.FLY_INTO_WALL){
                player.displayClientMessage(new TranslationTextComponent("You hit the wall with your head").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.RED), true);
                player.addEffect(new EffectInstance(Effects.BLINDNESS, 25, 4));
                player.addEffect(new EffectInstance(Effects.CONFUSION, (int) (event.getAmount() * 20)));
            }
        }
    }
}