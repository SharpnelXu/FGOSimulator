package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.Set;

@SuperBuilder
public class CardTypeChangeSelect extends GrantBuff {
    private final Set<CommandCardType> selections;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final CommandCardType selectedCardType = simulation.selectCommandCardType(selections);

        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final BuffData buffDataToUse = isBuffOvercharged ?
                        buffData.get(level - 1) :
                        buffData.get(0);

                final Buff buff = BuffFactory.buildBuff(
                        buffDataToUse.toBuilder().setStringValue(selectedCardType.name()).build(),
                        buffLevel
                );
                simulation.setCurrentBuff(buff);

                final double probability;
                if (!probabilities.isEmpty()) {
                    probability = isProbabilityOvercharged ?
                            probabilities.get(level - 1) :
                            probabilities.get(0);
                } else {
                    probability = 1;
                }

                grantBuff(simulation, buff, combatant, probability);

                simulation.setCurrentBuff(null);
            }

            simulation.setEffectTarget(null);
        }
    }
}
