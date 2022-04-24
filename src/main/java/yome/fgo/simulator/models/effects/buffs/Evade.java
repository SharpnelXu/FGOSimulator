package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Evade extends Buff {
    @Override
    public boolean isBuff() {
        if (forceBuff < 0) {
            return false;
        } else if (forceBuff > 0) {
            return true;
        } else {
            return true;
        }
    }

    @Override
    public boolean isDebuff() {
        if (forceBuff < 0) {
            return true;
        } else if (forceBuff > 0) {
            return false;
        } else {
            return false;
        }
    }
}
