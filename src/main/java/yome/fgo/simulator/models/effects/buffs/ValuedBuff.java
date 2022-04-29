package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;

@SuperBuilder
public abstract class ValuedBuff extends Buff {
    protected final double value;
    protected final double increaseValueEachTurn;
    protected final boolean isIncreasing;

    public double getValue(final Simulation simulation) {
        if (isIncreasing) {
            return RoundUtils.roundNearest(value + increaseValueEachTurn * turnPassed);
        } else {
            return value;
        }
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
