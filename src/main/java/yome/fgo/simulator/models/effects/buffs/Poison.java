package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Poison extends ValuedBuff {
    @Override
    protected boolean commonBuffCondition() {
        return false;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return true;
    }
}
