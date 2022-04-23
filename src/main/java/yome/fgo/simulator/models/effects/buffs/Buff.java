package yome.fgo.simulator.models.effects.buffs;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@Getter
@SuperBuilder
public class Buff {
    // set to -1 to be active forever (infinite turns or infinite times or both)
    @Builder.Default
    private int numTurnsActive = -1;
    @Builder.Default
    private int numTimesActive = -1;

    @Builder.Default
    private final Condition condition = ALWAYS;

    private final double value;

    public boolean isActive() {
        return numTimesActive != 0 && numTurnsActive != 0;
    }

    public boolean shouldApply(final Simulation simulation) {
        return condition.evaluate(simulation) && isActive();
    }

    public void applyOnce() {
        if (numTimesActive > 0) {
            numTimesActive -= 1;
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
