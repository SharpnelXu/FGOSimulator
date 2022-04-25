package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SpecialInvincible extends Buff implements DefenderBuff {
    @Override
    protected boolean commonBuffCondition() {
        return true;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return false;
    }
}
