package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.conditions.TargetsHaveBuff;
import yome.fgo.simulator.models.conditions.TargetsHaveTrait;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.BuffChanceBuff;
import yome.fgo.simulator.models.effects.buffs.Charm;
import yome.fgo.simulator.models.effects.buffs.DebuffChanceBuff;
import yome.fgo.simulator.models.effects.buffs.DebuffResist;
import yome.fgo.simulator.models.effects.buffs.ReceivedBuffChanceBuff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Target.EFFECT_TARGET;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.translation.Traits.DEMONIC;
import static yome.fgo.simulator.translation.Traits.RIDING;

public class GrantBuffTest {
    @Test
    public void testGrantBuff_regular() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(GrantBuff.class.getSimpleName())
                .setTarget(SELF)
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(AttackBuff.class.getSimpleName())
                                .addAllValues(ImmutableList.of(10.0, 11.0, 12.0, 13.0, 15.0))
                )
                .build();

        final Effect effect = EffectFactory.buildEffect(effectData, 5);

        final Simulation simulation = new Simulation();

        final Servant servant = new Servant("", CombatantData.newBuilder().build());
        simulation.setActivator(servant);
        effect.apply(simulation);

        final double attackBuff = servant.applyBuff(simulation, AttackBuff.class);
        assertEquals(15.0, attackBuff);
    }

    @Test
    public void testGrantBuff_conditional() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(GrantBuff.class.getSimpleName())
                .setTarget(SELF)
                .setApplyCondition(
                        ConditionData.newBuilder()
                                .setType(TargetsHaveTrait.class.getSimpleName())
                                .setTarget(EFFECT_TARGET)
                                .setValue(DEMONIC)
                )
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(AttackBuff.class.getSimpleName())
                                .addAllValues(ImmutableList.of(10.0, 11.0, 12.0, 13.0, 15.0))
                )
                .build();

        final Effect effect = EffectFactory.buildEffect(effectData, 5);

        final Simulation simulation = new Simulation();

        final Servant demonic = new Servant("", CombatantData.newBuilder().addTraits(DEMONIC).build());
        simulation.setActivator(demonic);
        effect.apply(simulation);

        final double attackBuff1 = demonic.applyBuff(simulation, AttackBuff.class);
        assertEquals(15.0, attackBuff1);

        final Servant nonDemonic = new Servant("", CombatantData.newBuilder().addTraits(RIDING).build());
        simulation.setActivator(nonDemonic);
        effect.apply(simulation);

        final double attackBuff2 = nonDemonic.applyBuff(simulation, AttackBuff.class);
        assertEquals(0, attackBuff2);
    }

    @Test
    public void testGrantBuff_overcharged() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(GrantBuff.class.getSimpleName())
                .setTarget(SELF)
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(AttackBuff.class.getSimpleName())
                                .addAllValues(ImmutableList.of(10.0, 11.0, 12.0, 13.0, 15.0))
                )
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(AttackBuff.class.getSimpleName())
                                .addAllValues(ImmutableList.of(20.0, 21.0, 22.0, 23.0, 25.0))
                )
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(AttackBuff.class.getSimpleName())
                                .addAllValues(ImmutableList.of(30.0, 31.0, 32.0, 33.0, 35.0))
                )
                .setIsOverchargedEffect(true)
                .build();

        final Effect effect = EffectFactory.buildEffect(effectData, 5);

        final Simulation simulation = new Simulation();

        final Servant servant = new Servant("", CombatantData.newBuilder().build());
        simulation.setActivator(servant);
        effect.apply(simulation, 3);

        final double attackBuff = servant.applyBuff(simulation, AttackBuff.class);
        assertEquals(35.0, attackBuff);
    }
    @Test
    public void testGrantBuff_buffProbability() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(GrantBuff.class.getSimpleName())
                .setTarget(SELF)
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(AttackBuff.class.getSimpleName())
                                .addAllValues(ImmutableList.of(10.0, 11.0, 12.0, 13.0, 15.0))
                )
                .addProbabilities(0.8)
                .build();

        final Effect effect = EffectFactory.buildEffect(effectData, 5);

        final Simulation simulation = new Simulation();
        simulation.setProbabilityThreshold(0.9);

        final Servant servant = new Servant("", CombatantData.newBuilder().build());
        simulation.setActivator(servant);
        effect.apply(simulation);

        final double attackBuff = servant.applyBuff(simulation, AttackBuff.class);
        assertEquals(0, attackBuff);

        servant.addBuff(BuffChanceBuff.builder().value(0.05).build());
        effect.apply(simulation);
        final double attackBuff2 = servant.applyBuff(simulation, AttackBuff.class);
        assertEquals(0, attackBuff2);

        servant.addBuff(ReceivedBuffChanceBuff.builder().value(0.05).build());
        effect.apply(simulation);
        final double attackBuff3 = servant.applyBuff(simulation, AttackBuff.class);
        assertEquals(15, attackBuff3);
    }
    @Test
    public void testGrantBuff_debuffProbability() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(GrantBuff.class.getSimpleName())
                .setTarget(SELF)
                .addBuffData(
                        BuffData.newBuilder()
                                .setType(Charm.class.getSimpleName())
                )
                .addProbabilities(0.8)
                .build();

        final Effect effect = EffectFactory.buildEffect(effectData, 5);

        final Simulation simulation = new Simulation();
        simulation.setProbabilityThreshold(0.9);

        final Servant servant = new Servant("", CombatantData.newBuilder().build());
        simulation.setActivator(servant);
        effect.apply(simulation);

        assertFalse(TargetsHaveBuff.builder().targetBuffType(Charm.class).target(SELF).build().evaluate(simulation));

        servant.addBuff(DebuffChanceBuff.builder().value(0.1).build());
        effect.apply(simulation);

        assertTrue(TargetsHaveBuff.builder().targetBuffType(Charm.class).target(SELF).build().evaluate(simulation));

        servant.getBuffs().clear();
        assertFalse(TargetsHaveBuff.builder().targetBuffType(Charm.class).target(SELF).build().evaluate(simulation));
        servant.addBuff(DebuffResist.builder().value(0.05).build());
        effect.apply(simulation);

        assertFalse(TargetsHaveBuff.builder().targetBuffType(Charm.class).target(SELF).build().evaluate(simulation));
    }
}
