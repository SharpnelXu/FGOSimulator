package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Confusion extends EndOfTurnEffect implements MentalDebuff {
    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return true;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }
}
