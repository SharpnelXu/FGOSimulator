package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class ValuedBuff extends Buff {
    protected final double value;

    public double getValue(final Simulation simulation) {
        return value;
    }

    @Override
    protected boolean commonBuffCondition() {
        return value > 0;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return value < 0;
    }
}
