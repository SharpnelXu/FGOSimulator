package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BurnEffectivenessUp extends ValuedBuff implements DamageOverTime {
    @Override
    public boolean commonBuffCondition() {
        return super.commonDebuffCondition();
    }

    @Override
    public boolean commonDebuffCondition() {
        return super.commonBuffCondition();
    }
}
