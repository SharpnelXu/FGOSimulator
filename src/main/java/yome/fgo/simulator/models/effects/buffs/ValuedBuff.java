package yome.fgo.simulator.models.effects.buffs;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.variations.Variation;

import java.text.NumberFormat;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public abstract class ValuedBuff extends Buff {
    @Builder.Default
    protected final Variation variation = NO_VARIATION;
    protected final double addition;

    protected double value; // no longer final due to effectiveness being a thing

    public void scaleValue(final double effectiveness) {
        this.value *= effectiveness;
    }

    public double getValue(final Simulation simulation) {
        simulation.setCurrentBuff(this);
        final double result = variation.evaluate(simulation, value, addition);
        simulation.unsetCurrentBuff();
        return result;
    }

    @Override
    public boolean commonBuffCondition() {
        return value > 0;
    }

    @Override
    public boolean commonDebuffCondition() {
        return value < 0;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }

    @Override
    public String toString() {
        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        String base = ": " + numberFormat.format(value);
        if (variation != NO_VARIATION) {
            base = base + " + " + numberFormat.format(addition) + " " + variation;
        }
        return super.toString() + base;
    }
}
