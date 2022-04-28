package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OverchargeBuff extends Buff {
    private final int value;

    @Override
    protected boolean commonBuffCondition() {
        return value > 0;
    }

    @Override
    protected boolean commonDebuffCondition() {
        return value < 0;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }
}
