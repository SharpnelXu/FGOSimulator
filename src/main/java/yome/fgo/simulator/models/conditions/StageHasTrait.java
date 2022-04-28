package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.GrantStageTrait;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS;

@AllArgsConstructor
public class StageHasTrait implements Condition {
    private final String trait;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final boolean stageHasTrait = simulation.getLevel()
                .getStage(simulation.getCurrentStage())
                .getTraits()
                .stream()
                .anyMatch(trait::equalsIgnoreCase);
        if (stageHasTrait) {
            return true;
        }

        for (final Combatant combatant : TargetUtils.getTargets(simulation, ALL_CHARACTERS)) {
            for (final Buff buff : combatant.getBuffs()) {
                if (buff instanceof GrantStageTrait && buff.shouldApply(simulation)) {
                    if (trait.equalsIgnoreCase(((GrantStageTrait) buff).getTrait())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
