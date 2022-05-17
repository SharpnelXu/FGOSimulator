package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class AscensionChange extends IntValuedEffect {
    private final Target target;

    @Override
    protected void internalApply(final  Simulation simulation, final int level) {

        final int value = getValue(simulation, level);
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (!(combatant instanceof Servant)) {
                continue;
            }

            simulation.setEffectTarget(combatant);
            final Servant servant = (Servant) combatant;
            servant.changeAscension(simulation, value);
            simulation.unsetEffectTarget();
        }
    }

    @Override
    public String toString() {
        return getTranslation(TARGET_SECTION, target.name()) + super.toString();
    }
}
