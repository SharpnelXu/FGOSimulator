package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BurnEffectivenessUp extends ValuedBuff {
    @Override
    protected boolean commonBuffCondition() {
        return super.commonDebuffCondition();
    }

    @Override
    protected boolean commonDebuffCondition() {
        return super.commonBuffCondition();
    }
}
