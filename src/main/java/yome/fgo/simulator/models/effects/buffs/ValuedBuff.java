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
    public boolean isBuff() {
        if (forceBuff < 0) {
            return false;
        } else if (forceBuff > 0) {
            return true;
        } else {
            return value > 0;
        }
    }

    @Override
    public boolean isDebuff() {
        if (forceBuff < 0) {
            return true;
        } else if (forceBuff > 0) {
            return false;
        } else {
            return value < 0;
        }
    }
}
