package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Curse extends ValuedBuff implements DamageOverTime {
    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return true;
    }

    public static Class<? extends ValuedBuff> getEffectivenessClass() {
        return CurseEffectivenessUp.class;
    }
}
