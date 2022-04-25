package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffChanceBuff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.DebuffChanceBuff;
import yome.fgo.simulator.models.effects.buffs.DebuffResist;
import yome.fgo.simulator.models.effects.buffs.ReceivedBuffChanceBuff;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class GrantBuff extends Effect {
    private final List<BuffData> buffData;
    private final Target target;
    private final int buffLevel;

    @Builder.Default
    private final double probability = 1;

    @Override
    public void apply(Simulation simulation, int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final Buff buff = BuffFactory.buildBuff(buffData.get(level - 1), buffLevel);
                simulation.setCurrentBuffToApply(buff);

                final double activationProbability;
                if (buff.isDebuff()) {
                    final double debuffChance = simulation.getActivator().applyBuff(simulation, DebuffChanceBuff.class);
                    final double debuffResist = combatant.applyBuff(simulation, DebuffResist.class);

                    activationProbability = RoundUtils.roundNearest(probability + debuffChance - debuffResist);
                } else if (buff.isBuff()) {
                    final double buffChance = simulation.getActivator().applyBuff(simulation, BuffChanceBuff.class);
                    final double receivedBuffChance = combatant.applyBuff(simulation, ReceivedBuffChanceBuff.class);

                    activationProbability = RoundUtils.roundNearest(probability + buffChance + receivedBuffChance);
                } else {
                    activationProbability = probability;
                }

                if (activationProbability >= simulation.getProbabilityThreshold()) {
                    combatant.addBuff(buff);
                }

                simulation.setCurrentBuffToApply(null);
            }
            simulation.setEffectTarget(null);
        }
    }
}
