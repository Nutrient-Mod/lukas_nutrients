package net.lukasllll.lukas_nutrients.event;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.api.event.NutrientEffectEvent;
import net.lukasllll.lukas_nutrients.commands.NutrientsCommand;
import net.lukasllll.lukas_nutrients.gameTests.GameTestCommons;
import net.lukasllll.lukas_nutrients.gameTests.tests.CommandTests;
import net.lukasllll.lukas_nutrients.gamerule.ModGameRules;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsAddToastS2CPacket;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffects;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onAddedNutrientEffect(NutrientEffectEvent.Added event) {
        if(!event.isSetup()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ModMessages.sendToPlayer(new NutrientsAddToastS2CPacket(event.getEffect(), true), player);
        }
    }

    @SubscribeEvent
    public static void onRemovedNutrientEffect(NutrientEffectEvent.Removed event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ModMessages.sendToPlayer(new NutrientsAddToastS2CPacket(event.getEffect(), false), player);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new NutrientsCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof ServerPlayer player) {
            if(!player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).isPresent()) {
                event.addCapability(new ResourceLocation(LukasNutrients.MOD_ID, "nutrient_data"), new PlayerNutrientProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if(event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {

                    nutrients.recalculateAll();
                    nutrients.updateClient(player);

                    int previousMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();

                    NutrientEffects.removeAll(player);
                    NutrientEffects.apply(player, previousMaxHealth, true);
                });
                NutrientManager.updateClient(player);
            }
        }
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        FoodNutrientProvider.assignUnassignedItems();
    }

    @SubscribeEvent
    public static  void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NutrientEffects.remove(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.getEntity() instanceof ServerPlayer player && event.isWasDeath()) {
            GameRules gamerules = event.getEntity().level().getLevelData().getGameRules();
            boolean keepNutrients = gamerules.getBoolean(ModGameRules.RULE_KEEPNUTRIENTS);
            if(keepNutrients) {
                event.getOriginal().reviveCaps();
                event.getOriginal().getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(oldNutrients -> {
                            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(newNutrients -> {
                                newNutrients.copyFrom(oldNutrients);
                            });
                });
                event.getOriginal().invalidateCaps();
            }

            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.updateClient(player);
                NutrientEffects.apply(player, true);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.SERVER) {
            ServerPlayer player = (ServerPlayer) event.player;
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.handleNutrientDecay(player);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        if(event.getEntity() instanceof ServerPlayer player && event.getItem().isEdible()) {
            if(((INutrientPropertiesHaver) event.getItem().getItem()).hasFoodNutrientProperties()) {
                NutrientProperties properties = ((INutrientPropertiesHaver) event.getItem().getItem()).getFoodNutrientProperties();

                player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                    nutrients.addAmounts(properties.getNutrientAmounts(), properties.getServings());
                    nutrients.updateClient(player);
                    NutrientEffects.apply(player, false);
                });
            }
        }
    }

    @SubscribeEvent
    // this exists to account for edible blocks such as cake
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();

        if(event.getEntity() instanceof ServerPlayer player && ((INutrientPropertiesHaver) block).hasFoodNutrientProperties()) {
            NutrientProperties properties = ((INutrientPropertiesHaver) block).getFoodNutrientProperties();

            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.addAmounts(properties.getNutrientAmounts(), properties.getServings());
                nutrients.updateClient(player);
                NutrientEffects.apply(player, false);
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event){
        event.register(CommandTests.class);
        event.register(GameTestCommons.class);
    }

}
