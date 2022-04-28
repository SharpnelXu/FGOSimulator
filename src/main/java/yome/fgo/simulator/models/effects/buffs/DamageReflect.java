package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class DamageReflect extends ValuedBuff {
    private int storedDamage;

    public void storeDamage(final int damage) {
        this.storedDamage += damage;
    }

    @Override
    protected boolean commonBuffCondition() {
        return false;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return false;
    }

    public void resetDamageStored() {
        storedDamage = 0;
    }
}
