package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public abstract class ValuedBuff extends Buff {
    protected final double value;

    public double getValue(final Simulation simulation) {
        return value;
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
