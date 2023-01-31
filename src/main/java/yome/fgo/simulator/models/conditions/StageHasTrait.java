package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

import java.util.Set;

import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class StageHasTrait extends Condition {
    private final String trait;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final Set<String> fieldTraits = simulation.getStageTraits();
        return fieldTraits.stream().anyMatch(trait::equalsIgnoreCase);
    }

    @Override
    public String toString() {
        return String.format(
                getTranslation(CONDITION_SECTION, "%s Have %s"),
                getTranslation(CONDITION_SECTION, "Stage"),
                getTranslation(TRAIT_SECTION, trait)
        );
    }
}
