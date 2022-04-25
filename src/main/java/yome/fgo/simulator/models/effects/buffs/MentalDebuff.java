package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class MentalDebuff extends Buff {
    @Override
    public boolean isBuff() {
        if (forceBuff < 0) {
            return false;
        } else return forceBuff > 0;
    }

    @Override
    public boolean isDebuff() {
        if (forceBuff < 0) {
            return true;
        } else return forceBuff <= 0;
    }
}
