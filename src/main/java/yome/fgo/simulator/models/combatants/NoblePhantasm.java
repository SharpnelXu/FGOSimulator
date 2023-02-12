package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmType;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.conditions.ConditionFactory;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;
import yome.fgo.simulator.models.effects.NoblePhantasmDamage;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.NoblePhantasmType.ANY_NP_TYPE;
import static yome.fgo.data.proto.FgoStorageData.Target.TARGETED_ENEMY;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@Getter
public class NoblePhantasm extends CommandCard {
    public static final NoblePhantasm ENEMY_DEFAULT_NOBLE_PHANTASM =
            new NoblePhantasm(BUSTER, List.of(100), 0, 0, List.of(
                    NoblePhantasmDamage.builder()
                            .target(TARGETED_ENEMY)
                            .damageRates(List.of(3.0))
                            .build()
            ), ANY_NP_TYPE, ALWAYS);

    private final List<Effect> effects;
    private final NoblePhantasmType noblePhantasmType;
    private final Condition activationCondition;

    public NoblePhantasm(
            final CommandCardType commandCardType,
            final List<Integer> hitPercentages,
            final double npCharge,
            final double starGeneration,
            final List<Effect> effects,
            final NoblePhantasmType noblePhantasmType,
            final Condition activationCondition
    ) {
        super(CommandCardData.newBuilder()
                     .setCommandCardType(commandCardType)
                     .addAllHitsData(hitPercentages)
                     .setNpRate(npCharge)
                     .setCriticalStarGen(starGeneration)
                     .build());
        this.effects = effects;
        this.noblePhantasmType = noblePhantasmType;
        this.activationCondition = activationCondition;
    }

    public NoblePhantasm(final NoblePhantasmData noblePhantasmData, final int noblePhantasmLevel) {
        super(noblePhantasmData.getCommandCardData());
        this.effects = EffectFactory.buildEffects(noblePhantasmData.getEffectsList(), noblePhantasmLevel);
        this.noblePhantasmType = noblePhantasmData.getNoblePhantasmType();
        if (noblePhantasmData.hasActivationCondition()) {
            this.activationCondition = ConditionFactory.buildCondition(noblePhantasmData.getActivationCondition());
        } else {
            this.activationCondition = ALWAYS;
        }
    }

    public void activate(final Simulation simulation, final int overchargeLevel) {
        for (final Effect effect : effects) {
            if (effect.isOverchargedEffect() || effect.isProbabilityOvercharged()) {
                effect.apply(simulation, overchargeLevel);
            } else {
                effect.apply(simulation);
            }
        }

        simulation.checkBuffStatus();
    }

    public boolean canActivate(final Simulation simulation) {
        return activationCondition.evaluate(simulation);
    }
}
