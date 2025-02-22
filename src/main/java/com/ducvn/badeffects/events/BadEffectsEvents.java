package com.ducvn.badeffects.events;

import com.ducvn.badeffects.config.BadEffectsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class BadEffectsEvents {

    private static int onFireTick = 0;
    private static int aboveBuildLimitTick = 0;
    private static List<Item> hotItem = new ArrayList<>(
            Arrays.asList(
                    Items.MAGMA_BLOCK.asItem(), Items.TORCH.asItem(), Items.LANTERN.asItem(),
                    Items.JACK_O_LANTERN.asItem(), Items.LAVA_BUCKET.asItem()
            )
    );
    private static int onColdBiomeTick = 0;
    private static List<Block> hotBlock = new ArrayList<>(
            Arrays.asList(
                    Blocks.TORCH, Blocks.MAGMA_BLOCK, Blocks.LANTERN,
                    Blocks.JACK_O_LANTERN, Blocks.LAVA
            )
    );
    private static int lightningTick = 0;
    private static List<Item> rawFood = new ArrayList<>(
            Arrays.asList(
                    Items.POTATO, Items.CARROT, Items.APPLE, Items.PORKCHOP, Items.COD, Items.SALMON,
                    Items.TROPICAL_FISH, Items.MELON_SLICE, Items.BEEF, Items.CHICKEN, Items.RABBIT,
                    Items.MUTTON, Items.BEETROOT, Items.SWEET_BERRIES, Items.ROTTEN_FLESH
            )
    );

    @SubscribeEvent
    public static void GiveHeatstrokeEvent(TickEvent.PlayerTickEvent event){
        Level world = event.player.level();
        if (!world.isClientSide && world.getBiome(event.player.blockPosition()).get().getModifiedClimateSettings().temperature() > 1.7F
                && !BadEffectsConfig.heatstroke.get()){
            Player player = event.player;
            if (player.isOnFire()){
                onFireTick++;
                if (onFireTick % 100 == 1 && onFireTick != 1){
                    player.displayClientMessage(Component.literal("You got heatstroke").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW),true);
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
                    Random roll = new Random();
                    if (roll.nextBoolean()){
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
                    }
                    else {
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100));
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
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.broke_leg.get()){
            Player player = (Player) event.getEntity();
            if ((event.getSource().type().equals(DamageTypes.FALL)) && event.getAmount() >= 2.0F){
                player.displayClientMessage(Component.literal("You hurt your legs").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED),true);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 + ((int) event.getAmount() - 2) * 20));
                if (event.getAmount() >= 10){
                    player.displayClientMessage(Component.literal("Your legs are broken").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED),true);
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 + ((int) event.getAmount() - 6) * 20));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveLackOfOxygenEvent(TickEvent.PlayerTickEvent event){
        Level world = event.player.level();
        if (!world.isClientSide && !BadEffectsConfig.lack_of_oxygen.get()){
            Player player = event.player;
            if (player.getY() >= 256){
                aboveBuildLimitTick++;
                if (aboveBuildLimitTick > 100){
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION));
                    if (aboveBuildLimitTick > 200){
                        player.displayClientMessage(Component.literal("You need oxygen").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE),true);
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER));
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
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.nausea_drowning.get()){
            Player player = (Player) event.getEntity();
            if (event.getSource().type().equals(DamageTypes.DROWN)){
                Random roll = new Random();
                if (roll.nextDouble() < 0.34D) {
                    player.displayClientMessage(Component.literal("You're drowning").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA), true);
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveFrostBiteEvent(TickEvent.PlayerTickEvent event){
        Level world = event.player.level();
        if (!world.isClientSide && world.getBiome(event.player.blockPosition()).get().getModifiedClimateSettings().temperature() < 0.6F
                && !BadEffectsConfig.frostbite.get()){
            Player player = event.player;
            if (!hotItem.contains(player.getItemInHand(InteractionHand.MAIN_HAND).getItem())
                    && !hotItem.contains(player.getItemInHand(InteractionHand.OFF_HAND).getItem())
                    && !isHotSourceAround(event.player.blockPosition(), world)){
                onColdBiomeTick++;
                if (onColdBiomeTick >= 1200){
                    if (onColdBiomeTick == 1200) {
                        player.displayClientMessage(Component.literal("You start to feel hungry").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_GREEN), true);
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER));
                    if ((onColdBiomeTick - 1700) % 100 == 0 && onColdBiomeTick > 1700) {
                        player.displayClientMessage(Component.literal("You got frostbite").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA), true);
                        player.hurt(player.damageSources().wither(), 2.0F + (float) (onColdBiomeTick - 1800) / 600F);
                    }
                }
            }
            else {
                onColdBiomeTick = 0;
            }
        }
    }

    private static boolean isHotSourceAround(BlockPos pos, Level world){
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
        Level world = event.getEntity().level();
        if(!world.isClientSide && event.getEntity() instanceof Player && event.getItem().isEdible()
                && !BadEffectsConfig.food_poisoning.get()){
            Player player = (Player) event.getEntity();
            if (player.getUseItemRemainingTicks() == 1){
                double triggerChance = 0.3D;
                if (rawFood.contains(event.getItem().getItem())){
                    triggerChance = 0.75D;
                }
                Random roll = new Random();
                if (roll.nextDouble() < triggerChance){
                    player.displayClientMessage(Component.literal("You just got food poisoning").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_GREEN), true);
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 300));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveStuckEvent(LivingDamageEvent event){
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.stuck.get()){
            Player player = (Player) event.getEntity();
            if (event.getSource().type().equals(DamageTypes.IN_WALL)){
                Random roll = new Random();
                if (roll.nextDouble() < 0.1D){
                    player.displayClientMessage(Component.literal("You're stuck and hard to move").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GRAY), true);
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveStunEvent(LivingDamageEvent event){
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.stun.get()){
            Player player = (Player) event.getEntity();
            if (event.getSource().type().equals(DamageTypes.EXPLOSION)){
                player.displayClientMessage(Component.literal("You are stunned by the explosion").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_PURPLE), true);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (event.getAmount() * 10), 5));
                if (event.getAmount() >= 10F){
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveKarmaEvent(LivingDeathEvent event){
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getSource().getEntity() instanceof Player
                && (event.getEntity() instanceof Villager || event.getEntity() instanceof IronGolem)
                && !BadEffectsConfig.karma.get()){
            Player player = (Player) event.getSource().getEntity();
            Random roll = new Random();
            if (roll.nextBoolean()){
                player.displayClientMessage(Component.literal("You will pay for what you did").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED), true);
                player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 36000));
            }
        }
    }

    @SubscribeEvent
    public static void GiveLightningStrikeEvent(TickEvent.PlayerTickEvent event){
        Level world = event.player.level();
        if (!world.isClientSide && !BadEffectsConfig.karma.get()){
            Player player = event.player;
            if (player.getEffect(MobEffects.UNLUCK) != null){
                lightningTick++;
                if (lightningTick % 100 == 0){
                    Random roll = new Random();
                    if (roll.nextDouble() < 0.1D){
                        player.displayClientMessage(Component.literal("This is your karma").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_PURPLE), true);
                        LightningBolt lightningBoltEntity = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
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
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.vision_disrupt.get()){
            Player player = (Player) event.getEntity();
            if (event.getSource().type().equals(DamageTypes.MAGIC) && player.getEffect(MobEffects.POISON) == null){
                Random roll = new Random();
                if (roll.nextDouble() < 0.34D){
                    player.displayClientMessage(Component.literal("Your vision got disrupted by magic").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_PURPLE), true);
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 4));
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveBrokenHandEvent(BlockEvent.BreakEvent event){
        Level world = event.getPlayer().level();
        if (!world.isClientSide && event.getPlayer() instanceof Player
                && !BadEffectsConfig.broken_hand.get()){
            BlockState blockState = world.getBlockState(event.getPos());
            if (blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("minecraft:logs")))
                    || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("forge:logs")))
                    || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("minecraft:glass")))
                    || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("forge:glass")))
                    || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("minecraft:glass_panes")))
                    || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("forge:glass_panes")))){
                Player player = event.getPlayer();
                if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.AIR){
                    Random roll = new Random();
                    if (roll.nextDouble() < 0.34D){
                        if (blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("minecraft:glass")))
                                || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("forge:glass")))
                                || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("minecraft:glass_panes")))
                                || blockState.getTags().toList().contains(new TagKey<>(Registries.BLOCK, new ResourceLocation("forge:glass_panes")))){
                            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100));
                        }
                        player.displayClientMessage(Component.literal("You hurt your hand").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED), true);
                        player.hurt(player.damageSources().wither(), 2);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveInfectionEvent(LivingDamageEvent event){
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.infection.get()){
            if (event.getSource().equals(DamageTypes.CACTUS)
                    || event.getSource().equals(DamageTypes.SWEET_BERRY_BUSH)){
                Player player = (Player) event.getEntity();
                if (player.getArmorCoverPercentage() < 1.0f){
                    double adjustment = 0;
                    if (event.getSource().equals(DamageTypes.SWEET_BERRY_BUSH)){
                        if (player.hasItemInSlot(EquipmentSlot.FEET)){
                            adjustment = adjustment + 0.5D;
                        }
                        if (player.hasItemInSlot(EquipmentSlot.LEGS)){
                            adjustment = adjustment + 0.5D;
                        }
                    }
                    if (event.getSource().equals(DamageTypes.CACTUS)){
                        adjustment = player.getArmorCoverPercentage();
                    }
                    Random roll = new Random();
                    if (roll.nextDouble() < (1D - 0.6D - 0.4D * adjustment)){
                        player.displayClientMessage(Component.literal("You should wear more armor").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_GREEN), true);
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void GiveHeadBumpEvent(LivingDamageEvent event){
        Level world = event.getEntity().level();
        if (!world.isClientSide && event.getEntity() instanceof Player
                && !BadEffectsConfig.head_bump.get()){
            Player player = (Player) event.getEntity();
            if (event.getSource().equals(DamageTypes.FLY_INTO_WALL)){
                player.displayClientMessage(Component.literal("You hit the wall with your head").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED), true);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 4));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (int) (event.getAmount() * 20)));
            }
        }
    }

    @SubscribeEvent
    public static void GiveExhaustedEvent(TickEvent.PlayerTickEvent event){
        Level world = event.player.level();
        if (!world.isClientSide && !BadEffectsConfig.exhausted.get() && (TickEvent.Phase.START == event.phase)){
            ServerStatsCounter serverstatscounter = ((ServerPlayer)event.player).getStats();
            int timeSinceRest = Mth.clamp(serverstatscounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
            if ((72000 <= timeSinceRest) && (timeSinceRest % 24000 == 0)){ // 1 day = 24000 ticks
                event.player.displayClientMessage(Component.literal("You stay awake for too long, you feel exhausted")
                        .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GRAY), true);
                switch ((timeSinceRest / 12000) % 3){
                    case 0:{
                        if (!event.player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)){
                            event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24000,0));
                        }
                        else {
                            event.player.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.MOVEMENT_SLOWDOWN,
                                            24000,
                                            event.player.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() + 1
                                    )
                            );
                        }
                        break;
                    }
                    case 1:{
                        if (!event.player.hasEffect(MobEffects.WEAKNESS)){
                            event.player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 24000,0));
                        }
                        else {
                            event.player.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.WEAKNESS,
                                            24000,
                                            event.player.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1
                                    )
                            );
                        }
                        break;
                    }
                    case 2:{
                        if (!event.player.hasEffect(MobEffects.DIG_SLOWDOWN)){
                            event.player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 24000,0));
                        }
                        else {
                            event.player.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.DIG_SLOWDOWN,
                                            24000,
                                            event.player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier() + 1
                                    )
                            );
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

}
