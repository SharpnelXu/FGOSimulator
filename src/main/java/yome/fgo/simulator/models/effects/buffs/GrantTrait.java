package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class GrantTrait extends Buff {
    private final String trait;

    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }
}
