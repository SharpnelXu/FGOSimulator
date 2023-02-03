package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class MaxHpBuff extends Buff {
    private int change;

    public void scaleValue(final double effectiveness) {
        change = (int) (change * effectiveness);
    }

    @Override
    public boolean commonBuffCondition() {
        return change > 0;
    }

    @Override
    public boolean commonDebuffCondition() {
        return change < 0;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }

    @Override
    public String toString() {
        String base = ": " + change;
        return super.toString() + base;
    }
}
