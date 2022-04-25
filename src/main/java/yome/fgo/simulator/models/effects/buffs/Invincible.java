package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Invincible extends Buff implements DefenderBuff {
    @Override
    protected boolean commonBuffCondition() {
        return true;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return false;
    }
}
