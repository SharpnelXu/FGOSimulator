package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Invincible extends Buff implements DefenderBuff {
    @Override
    public boolean commonBuffCondition() {
        return true;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return false;
    }
}
