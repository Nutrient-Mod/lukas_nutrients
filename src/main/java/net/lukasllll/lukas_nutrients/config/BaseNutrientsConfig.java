package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseNutrientsConfig{
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/base_nutrients.json";

    public static BaseNutrientsConfig DATA = null;
    public HashMap<String, List<String>> baseNutrients;


    public static void create() {
        DATA = getDefaultBaseNutrients();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try(FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(DATA, writer);
            LukasNutrients.LOGGER.debug("Successfully created " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void read() {
        File path = new File(FILE_PATH);
        if(!path.exists()) {
            LukasNutrients.LOGGER.debug(FILE_PATH + " doesn't exist!");
            create();
            return;
        }

        Gson gson = new Gson();

        try(FileReader reader = new FileReader(path)) {
            DATA = gson.fromJson(reader, BaseNutrientsConfig.class);
            LukasNutrients.LOGGER.debug("Successfully read " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BaseNutrientsConfig(HashMap<String, List<String>> baseNutrients) {
        this.baseNutrients = baseNutrients;
    }

    private static BaseNutrientsConfig getDefaultBaseNutrients() {
        HashMap<String, List<String>> map = new HashMap<>();

        String grains = "grains";       //maybe change? Values already saved in nutrients.NutrientGroup
        String fruits = "fruits";
        String vegetables = "vegetables";
        String proteins = "proteins";
        String sugars = "sugars";

        map.put(Registry.ITEM.getKey(Items.APPLE).toString(), buildEntry(0.9, fruits));
        map.put(Registry.ITEM.getKey(Items.CHORUS_FRUIT).toString(), buildEntry( 0.4, fruits));
        map.put(Registry.ITEM.getKey(Items.MELON_SLICE).toString(), buildEntry( 0.9, fruits));
        map.put(Registry.ITEM.getKey(Items.PUMPKIN).toString(), buildEntry( 1.0, fruits));
        map.put(Registry.ITEM.getKey(Items.SWEET_BERRIES).toString(), buildEntry( 0.9, fruits));
        map.put(Registry.ITEM.getKey(Items.GLOW_BERRIES).toString(), buildEntry( 0.9, fruits));
        map.put(Registry.ITEM.getKey(Items.ENCHANTED_GOLDEN_APPLE).toString(), buildEntry( 0.9, fruits));

        map.put(Registry.ITEM.getKey(Items.BREAD).toString(), buildEntry( 1.0, grains));
        map.put(Registry.ITEM.getKey(Items.WHEAT).toString(), buildEntry( 0.43, grains));

        map.put(Registry.ITEM.getKey(Items.CHICKEN).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.PORKCHOP).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.MUTTON).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.BEEF).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.RABBIT).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.COD).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.SALMON).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.TROPICAL_FISH).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.SPIDER_EYE).toString(), buildEntry( 0.9, proteins));
        map.put(Registry.ITEM.getKey(Items.MILK_BUCKET).toString(), buildEntry( 0.1, proteins));
        map.put(Registry.ITEM.getKey(Items.EGG).toString(), buildEntry( 0.5, proteins));
        map.put(Registry.ITEM.getKey(Items.ROTTEN_FLESH).toString(), buildEntry( 0.5, proteins));


        map.put(Registry.ITEM.getKey(Items.CARROT).toString(), buildEntry( 0.9, vegetables));
        map.put(Registry.ITEM.getKey(Items.BEETROOT).toString(), buildEntry( 0.9, vegetables));
        map.put(Registry.ITEM.getKey(Items.KELP).toString(), buildEntry( 0.1, vegetables));
        map.put(Registry.ITEM.getKey(Items.POTATO).toString(), buildEntry( 0.9, vegetables));
        map.put(Registry.ITEM.getKey(Items.POISONOUS_POTATO).toString(), buildEntry( 0.4, vegetables));
        map.put(Registry.ITEM.getKey(Items.MUSHROOM_STEW).toString(), buildEntry( 1.0, vegetables));
        map.put(Registry.ITEM.getKey(Items.RED_MUSHROOM).toString(), buildEntry( 0.54, vegetables));
        map.put(Registry.ITEM.getKey(Items.BROWN_MUSHROOM).toString(), buildEntry( 1.0, vegetables));


        map.put(Registry.ITEM.getKey(Items.HONEY_BOTTLE).toString(), buildEntry( 2.0, sugars));
        map.put(Registry.ITEM.getKey(Items.COCOA_BEANS).toString(), buildEntry( 0.6, sugars));

        return new BaseNutrientsConfig(map);
    }

    public static ArrayList<String> buildEntry(double nutrientEffectiveness, String... nutrientIDs) {
        ArrayList<String> out = new ArrayList<>();
        for(String id : nutrientIDs) {
            out.add(id);
        }
        out.add("" + nutrientEffectiveness);
        return out;
    }
}
