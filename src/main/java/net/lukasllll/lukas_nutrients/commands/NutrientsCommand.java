package net.lukasllll.lukas_nutrients.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.DietEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class NutrientsCommand {

    public NutrientsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nutrients").requires((command) -> {
            return command.hasPermission(2);
        }).then(Commands.literal("set").then(Commands.argument("nutrient id", StringArgumentType.string()).suggests(NutrientGroupSuggestionProvider.getProvider()).then(Commands.argument("amount", IntegerArgumentType.integer(0, 24)).executes((command) -> {
            return setNutrients(command.getSource(), StringArgumentType.getString(command, "nutrient id"), IntegerArgumentType.getInteger(command, "amount"));
        })))).then(Commands.literal("get").then(Commands.argument("nutrient id", StringArgumentType.string()).suggests(NutrientGroupSuggestionProvider.getProvider()).executes((command) -> {
            return getNutrients(command.getSource(), StringArgumentType.getString(command, "nutrient id"));
        }))).then(Commands.literal("list").executes((command) -> {
            return listNutrientGroups(command.getSource());
        })));
    }

    private int setNutrients(CommandSourceStack source, String nutrientID, int amount) {

        ServerPlayer player =source.getPlayer();
        player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
            nutrients.setAmount(nutrientID,amount);
            nutrients.updateClient(player);
            DietEffects.apply(player);
            player.sendSystemMessage(Component.literal("Set " + nutrients.getDisplayName(nutrientID) + " to " + amount));
        });

        return 1;
    }

    private int getNutrients(CommandSourceStack source, String nutrientID) {

        ServerPlayer player =source.getPlayer();
        player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
            double amount = nutrients.getNutrientAmount(nutrientID);
            player.sendSystemMessage(Component.literal(nutrients.getDisplayName(nutrientID) + ": " + amount));
        });

        return 1;
    }

    private int listNutrientGroups(CommandSourceStack source) {

        NutrientGroup[] Groups = NutrientGroup.getNutrientGroups();

        String message = "Food Groups: ";
        for(int i=0; i<Groups.length; i++) {
            message += Groups[i].getID();
            if(i!=Groups.length-1)
                message += ", ";
        }

        ServerPlayer player =source.getPlayer();
        player.sendSystemMessage(Component.literal(message));

        return 1;
    }

}
