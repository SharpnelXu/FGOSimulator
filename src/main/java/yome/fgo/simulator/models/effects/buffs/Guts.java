package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.text.NumberFormat;

@SuperBuilder
@Getter
public class Guts extends Buff {
    private final int gutsLeft;
    private final boolean isPercentageGuts;
    private final double percent;

    @Override
    public boolean commonBuffCondition() {
        return true;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    public boolean commonStackableCondition() {
        return false;
    }

    @Override
    public String toString() {
        if (isPercentageGuts) {
            final NumberFormat numberFormat = NumberFormat.getPercentInstance();
            numberFormat.setMaximumFractionDigits(2);
            String base = ": " + numberFormat.format(percent);
            return super.toString() + base;
        } else {
            String base = ": " + gutsLeft;
            return super.toString() + base;
        }
    }
}
