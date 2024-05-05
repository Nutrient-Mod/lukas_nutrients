package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Sum extends Operator implements IDisplayElement, ICalcElement {

    public Sum(String id, String displayname, int[] pointRanges, int basePoint, boolean score, String[] summandIDs) {
        super(id, displayname, pointRanges, basePoint, score, summandIDs);
    }

    public Sum(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public int getCurrentAmount(Iterator<Integer> inputAmounts, Iterator<Integer> inputScores) {
        AtomicInteger out = new AtomicInteger();
        inputScores.forEachRemaining(out::addAndGet);
        return out.get();
    }

    @Override
    public void calcMaxAmount() {
        maxAmount = 0;
        for(ICalcElement input : inputs) {
            int inputMaxScore = input.getMaxScore();
            if(inputMaxScore == -1 && input instanceof Operator) {
                ((Operator) input).calcMaxAmount();
                inputMaxScore = input.getMaxScore();
            }
            maxAmount += inputMaxScore;
        }
    }

    public DisplayBarStyle getDisplayBarStyle() {
        return DisplayBarStyle.SUM;
    }

    @Override
    public int getTextureStartX() {
        return 0;
    }
}
