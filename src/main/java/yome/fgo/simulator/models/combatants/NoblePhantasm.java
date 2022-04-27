package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmType;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.List;

@Getter
public class NoblePhantasm extends CommandCard {
    private final List<Effect> effects;
    private final NoblePhantasmType noblePhantasmType;

    public NoblePhantasm(
            final CommandCardType commandCardType,
            final List<Integer> hitPercentages,
            final double npCharge,
            final double starGeneration,
            final List<Effect> effects,
            final NoblePhantasmType noblePhantasmType
    ) {
        super(CommandCardData.newBuilder()
                     .setCommandCardType(commandCardType)
                     .addAllHitsData(hitPercentages)
                     .setNpRate(npCharge)
                     .setCriticalStarGen(starGeneration)
                     .build());
        this.effects = effects;
        this.noblePhantasmType = noblePhantasmType;
    }

    public NoblePhantasm(final NoblePhantasmData noblePhantasmData, final int noblePhantasmLevel) {
        super(noblePhantasmData.getCommandCardData());
        this.effects = EffectFactory.buildEffects(noblePhantasmData.getEffectsList(), noblePhantasmLevel);
        this.noblePhantasmType = noblePhantasmData.getNoblePhantasmType();
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
