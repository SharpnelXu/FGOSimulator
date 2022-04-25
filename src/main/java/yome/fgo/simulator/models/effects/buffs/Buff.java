package yome.fgo.simulator.models.effects.buffs;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@SuperBuilder
@Getter
public abstract class Buff {

    // set to -1 to be active forever (infinite turns or infinite times or both)
    @Builder.Default
    private int numTurnsActive = -1;
    @Builder.Default
    private int numTimesActive = -1;

    @Builder.Default
    private final Condition condition = ALWAYS;

    protected final int forceBuff;

    private final boolean irremovable;

    private boolean isApplied; // for correcting decreasing numTimesActive

    public boolean isInactive() {
        return numTimesActive == 0 || numTurnsActive == 0;
    }

    protected abstract boolean commonBuffCondition();

    protected abstract boolean commonDebuffCondition();

    public boolean isBuff() {
        if (forceBuff < 0) {
            return false;
        } else if (forceBuff > 0) {
            return true;
        } else {
            return commonBuffCondition();
        }
    }

    public boolean isDebuff() {
        if (forceBuff < 0) {
            return true;
        } else if (forceBuff > 0) {
            return false;
        } else {
            return commonDebuffCondition();
        }
    }

    public boolean shouldApply(final Simulation simulation) {
        return condition.evaluate(simulation) && !isInactive();
    }

    public void setApplied() {
        isApplied = true;
    }

    public void decreaseNumTimeActive() {
        isApplied = false;
        if (numTimesActive > 0) {
            numTimesActive -= 1;
        }
    }

    public void decreaseNumTurnsActive() {
        if (numTurnsActive > 0) {
            numTurnsActive -= 1;
        }
    }

    public boolean isPermanentEffect() {
        return isPermanentTurnEffect() && isPermanentTimeEffect();
    }

    public boolean isPermanentTurnEffect() {
        return this.numTurnsActive < 0;
    }

    public boolean isPermanentTimeEffect() {
        return this.numTimesActive < 0;
    }
}
