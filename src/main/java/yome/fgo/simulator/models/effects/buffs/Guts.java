package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class Guts extends Buff {
    private final int gutsLeft;
    private final boolean isPercentageGuts;
    private final double percent;

    @Override
    public boolean commonBuffCondition() {
        return true;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    public boolean commonStackableCondition() {
        return false;
    }
}
