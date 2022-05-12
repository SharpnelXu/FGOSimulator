package yome.fgo.simulator.models.combatants;

import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.GrantBuff;

public class AppendSkill extends Skill {
    public AppendSkill(final AppendSkillData appendSkillData, final int appendSkillLevel) {
        super(appendSkillData.getEffectsList(), appendSkillLevel);
    }

    public void activateOnlyBuffGrantingEffects(final Simulation simulation) {
        for (final Effect effect : effects) {
            if (effect instanceof GrantBuff) {
                effect.apply(simulation);
            }
        }

        simulation.checkBuffStatus();
    }
}
