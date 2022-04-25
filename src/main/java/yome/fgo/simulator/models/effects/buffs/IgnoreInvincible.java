package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class IgnoreInvincible extends Buff implements AttackerBuff {
    @Override
    protected boolean commonBuffCondition() {
        return true;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return false;
    }
}
