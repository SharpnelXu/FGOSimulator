package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.List;

@Getter
public class Skill {
    protected final List<Effect> effects;

    public Skill(final List<EffectData> effectDataList, final int skillLevel) {
        this.effects = EffectFactory.buildEffects(effectDataList, skillLevel);
    }

    public void activate(final Simulation simulation) {
        for (final Effect effect: effects) {
            effect.apply(simulation);
        }
    }
}
