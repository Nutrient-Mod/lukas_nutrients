package net.lukasllll.lukas_nutrients.nutrients.food;

import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;

public class NutrientProperties {
    //Through the ItemMixin class, every item now has NutrientProperties

    private double[] nutrientAmounts;
    private int servings;
    private boolean placeableEdible;        //whether the item can be placed to create an edible block e.g. cake

    public NutrientProperties(double[] nutrientAmounts) {
        this.nutrientAmounts = nutrientAmounts;
        this.placeableEdible = false;
        this.servings = 1;
    }

    public NutrientProperties(double[] nutrientAmounts, int servings) {
        this.nutrientAmounts = nutrientAmounts;
        this.placeableEdible = false;
        this.servings = servings;
    }

    public NutrientProperties(double[] nutrientAmounts, int servings, boolean placeableEdible) {
        this.nutrientAmounts = nutrientAmounts;
        this.placeableEdible = placeableEdible;
        this.servings = servings;
    }

    public void setPlaceableEdible(boolean b) { placeableEdible = b; }
    public void setServings(int n) { servings = n; }

    public double[] getNutrientAmounts() {
        return nutrientAmounts;
    }

    public double getNutrientAmount(int arrayIndex) {
        return nutrientAmounts[arrayIndex];
    }

    public int getServings() { return servings; }

    public boolean getPlaceableEdible() { return placeableEdible; }

    public double getNutrientAmount(String nutrientID) {
        int arrayIndex = NutrientManager.getNutrientArrayIndex(nutrientID);
        return getNutrientAmount(arrayIndex);
    }

    public NutrientProperties clone() {
        return new NutrientProperties(nutrientAmounts.clone(), this.servings, this.placeableEdible);
    }

}
