package yome.fgo.simulator.models.combatants;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;

import java.util.List;

public class ActiveSkill {
    public List<Effect> effects;

    public void activate(final Simulation simulation) {
        for (final Effect effect: effects) {
            effect.apply(simulation);
        }
    }
}
