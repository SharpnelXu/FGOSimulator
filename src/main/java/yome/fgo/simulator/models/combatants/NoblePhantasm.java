package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class NoblePhantasm extends CommandCard {
    private final List<Effect> effects;

    public NoblePhantasm(final NoblePhantasmData noblePhantasmData, final int noblePhantasmLevel) {
        super(noblePhantasmData.getCommandCardData());
        this.effects = noblePhantasmData.getEffectsList()
                .stream()
                .map(effectData -> EffectFactory.buildEffect(effectData, noblePhantasmLevel))
                .collect(Collectors.toList());
    }

    public void activate(final Simulation simulation, final int overchargeLevel) {
        for (final Effect effect : effects) {
            if (effect.isOverchargedEffect()) {
                effect.apply(simulation, overchargeLevel);
            } else {
                effect.apply(simulation);
            }
        }
    }
}
