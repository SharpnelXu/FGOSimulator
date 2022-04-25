package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Charm extends Buff implements MentalDebuff {
    @Override
    protected boolean commonBuffCondition() {
        return false;
    }
    @Override
    protected boolean commonDebuffCondition() {
        return true;
    }

    @Override
    protected boolean commonStackableCondition() {
        return false;
    }
}
