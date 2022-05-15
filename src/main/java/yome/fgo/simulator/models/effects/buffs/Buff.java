package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.conditions.Condition;

import java.text.NumberFormat;
import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
@Getter
public abstract class Buff {

    // set to -1 to be active forever (infinite turns or infinite times or both)
    @Builder.Default
    protected int numTurnsActive = -1;
    @Builder.Default
    protected int numTimesActive = -1;
    @Builder.Default
    protected int turnPassed = 0;

    @Builder.Default
    private final Condition condition = ALWAYS;

    protected final boolean forceStackable;

    private boolean irremovable;

    // used to correctly remove passive & append skills during ascension transition
    private boolean isPassive;

    private String iconName;

    private Combatant activator;

    @Builder.Default
    private final double probability = 1;

    private boolean isApplied; // for correcting decreasing numTimesActive

    @Builder.Default
    protected final List<String> buffTraits = Lists.newArrayList();

    @Override
    public String toString() {
        final String base = getTranslation(BUFF_SECTION, getClass().getSimpleName());
        return base + miscString();
    }

    public String durationString() {
        String base = "(";
        final boolean isPermanentTimes = isPermanentTimeEffect();
        final boolean isPermanentTurn = isPermanentTurnEffect();
        final String timesString = numTimesActive + getTranslation(BUFF_SECTION, "Times");
        final String turnString = numTurnsActive + getTranslation(BUFF_SECTION, "Turns");
        if (isPermanentTimes) {
            if (isPermanentTurn) {
                base = base + getTranslation(BUFF_SECTION, "Permanent");
            } else {
                base = base + turnString;
            }
        } else {
            base = base + timesString;
            if (!isPermanentTurn) {
                base = base + " " + turnString;
            }
        }
        base = base + ")";
        return base;
    }

    public String miscString() {
        String base = "";
        if (condition != ALWAYS) {
            base = base + " (" + condition + ")";
        }

        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        if (probability != 1) {
            base = base + " " + numberFormat.format(probability) + getTranslation(BUFF_SECTION, "Probability");
        }
        if (irremovable) {
            base = base + " " + getTranslation(BUFF_SECTION, "Irremovable");
        }
        if (forceStackable) {
            base = base + " " + getTranslation(BUFF_SECTION, "Force Stackable");
        }
        return base;
    }

    public boolean isInactive() {
        return numTimesActive == 0 || numTurnsActive == 0;
    }

    public abstract boolean commonBuffCondition();

    public abstract boolean commonDebuffCondition();

    protected abstract boolean commonStackableCondition();

    public boolean isStackable() {
        return forceStackable || commonStackableCondition();
    }

    public boolean isBuff() {
        return buffTraits.contains(POSITIVE_BUFF.name());
    }

    public boolean isDebuff() {
        return buffTraits.contains(NEGATIVE_BUFF.name());
    }

    public boolean shouldApply(final Simulation simulation) {
        return condition.evaluate(simulation) && simulation.getProbabilityThreshold() <= probability;
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
        turnPassed += 1;
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
