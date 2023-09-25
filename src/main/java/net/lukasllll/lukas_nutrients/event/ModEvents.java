package net.lukasllll.lukas_nutrients.event;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.commands.NutrientsCommand;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrients;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.DietEffects;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = LukasNutrients.MOD_ID)
public class ModEvents {



    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerNutrients.class);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new NutrientsCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }


    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).isPresent()) {
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
                    DietEffects.apply(player);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        //TODO does this belong here???
        FoodNutrientProvider.assignUnassignedItems();
    }

    @SubscribeEvent
    public static  void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DietEffects.remove(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.getEntity() instanceof ServerPlayer player && event.isWasDeath()) {
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.setToDefault();
                nutrients.updateClient(player);
                DietEffects.apply(player);
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
                double[] amounts = properties.getNutrientAmounts();
                String amountText = "";
                for(int i=0; i< amounts.length; i++) {
                    amountText += amounts[i] + (i == amounts.length-1 ? "" : ", ");
                }
                player.sendSystemMessage(Component.literal("Has FoodNutrientProperties: " + amountText));
                player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                    nutrients.addAmounts(properties.getNutrientAmounts());
                    nutrients.updateClient(player);
                    DietEffects.apply(player);
                });
            } else {
                player.sendSystemMessage(Component.literal("Doesn't have FoodNutrientProperties"));
            }
        }
    }

}
