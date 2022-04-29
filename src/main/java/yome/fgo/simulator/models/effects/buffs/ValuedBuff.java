package yome.fgo.simulator.models.effects.buffs;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.variations.Variation;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public abstract class ValuedBuff extends Buff {
    protected final double value;
    @Builder.Default
    protected final Variation variation = NO_VARIATION;
    protected final double addition;
    protected final boolean isIncreasing;

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
}
