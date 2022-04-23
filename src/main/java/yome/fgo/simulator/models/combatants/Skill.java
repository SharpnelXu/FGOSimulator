package yome.fgo.simulator.models.combatants;

import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.List;
import java.util.stream.Collectors;

public class Skill {
    protected final List<Effect> effects;

    public Skill(final List<EffectData> effectDataList, final int skillLevel) {
        this.effects = effectDataList.stream()
                .map(effectData -> EffectFactory.buildEffect(effectData, skillLevel))
                .collect(Collectors.toList());
    }

    public void activate(final Simulation simulation) {
        for (final Effect effect: effects) {
            effect.apply(simulation);
        }
    }
}
