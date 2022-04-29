package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class CharmResistDown extends DebuffResist implements MentalDebuff {
    // charm resist is only a mental debuff when negative
    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return true;
    }

    @Override
    public double getValue(final Simulation simulation) {
        if (value < 0) {
            throw new IllegalStateException("CharmResistDown should only contain negative value: " + value);
        }
        return -1 * value;
    }
}
