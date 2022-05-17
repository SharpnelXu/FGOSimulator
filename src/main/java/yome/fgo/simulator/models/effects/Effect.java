package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
@Getter
public abstract class Effect {
    protected final boolean isOverchargedEffect;
    @Builder.Default
    protected final Condition applyCondition = ALWAYS;

    protected final boolean isProbabilityOvercharged;

    @Builder.Default
    protected final List<Double> probabilities = Lists.newArrayList(1.0);

    protected double getProbability(final int level) {
        if (isProbabilityOvercharged) {
            return probabilities.get(level - 1);
        } else {
            return probabilities.get(0);
        }
    }

    protected boolean shouldApply(final Simulation simulation) {
        return applyCondition.evaluate(simulation);
    }

    public void apply(final Simulation simulation) {
        apply(simulation, 1);
    }

    public void apply(final Simulation simulation, final int level) {
        if (simulation.getStatsLogger() != null) {
            final String ocString = level != 1 ? "OC" + level : "";
            simulation.getStatsLogger().logEffect(ocString + this);
        }

        internalApply(simulation, level);
    }

    // for overcharged
    protected abstract void internalApply(final Simulation simulation, final int level);

    @Override
    public String toString() {
        final String base = getTranslation(EFFECT_SECTION, getClass().getSimpleName());
        return base + miscString();
    }

    public String miscString() {
        String base = "";
        if (applyCondition != ALWAYS) {
            base = base + " (" + applyCondition + ")";
        }

        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        if (isProbabilityOvercharged()) {
            base = base + " (OC) " + probabilities.stream().map(numberFormat::format).collect(Collectors.toList()) +
                    getTranslation(EFFECT_SECTION, "Probability");
        } else if (probabilities.get(0) != 1.0) {
            base = base + " " + numberFormat.format(probabilities.get(0)) +
                    getTranslation(EFFECT_SECTION, "Probability");
        }
        return base;
    }
}
