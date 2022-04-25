package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class GrantTrait extends Buff {
    private final String trait;

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
        return true;
    }

    @Override
    public boolean isBuff() {
        return forceBuff > 0;
    }

    @Override
    public boolean isDebuff() {
        return forceBuff < 0;
    }
}
