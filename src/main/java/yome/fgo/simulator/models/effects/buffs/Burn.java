package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Burn extends ValuedBuff implements EndOfTurnDamage {
    @Override
    protected boolean commonBuffCondition() {
        return false;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return true;
    }
}
