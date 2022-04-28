package yome.fgo.simulator.models.effects.buffs;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.conditions.Condition;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@SuperBuilder
@Getter
public abstract class Buff {

    // set to -1 to be active forever (infinite turns or infinite times or both)
    @Builder.Default
    protected int numTurnsActive = -1;
    @Builder.Default
    protected int numTimesActive = -1;

    @Builder.Default
    private final Condition condition = ALWAYS;

    protected final boolean forceStackable;

    protected final int forceBuff;

    private boolean irremovable;

    // used to correctly remove passive & append skills during ascension transition
    private boolean isPassive;
    private Combatant activator;

    @Builder.Default
    private final double probability = 1;

    private boolean isApplied; // for correcting decreasing numTimesActive

    public boolean isInactive() {
        return numTimesActive == 0 || numTurnsActive == 0;
    }

    protected abstract boolean commonBuffCondition();

    protected abstract boolean commonDebuffCondition();

    protected abstract boolean commonStackableCondition();

    public boolean isStackable() {
        return forceStackable || commonStackableCondition();
    }

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
        return condition.evaluate(simulation) && !isInactive() && simulation.getProbabilityThreshold() <= probability;
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

    public void setIrremovable(final boolean irremovable) {
        this.irremovable = irremovable;
    }

    public void setIsPassive(final boolean isPassive) {
        this.isPassive = isPassive;
    }

    public void setActivator(final Combatant activator) {
        this.activator = activator;
    }
}
