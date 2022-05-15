package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OverchargeBuff extends Buff {
    private final int value;

    @Override
    public boolean commonBuffCondition() {
        return value > 0;
    }

    @Override
    public boolean commonDebuffCondition() {
        return value < 0;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }

    @Override
    public String toString() {
        String base = ": " + value;
        return super.toString() + base;
    }
}
