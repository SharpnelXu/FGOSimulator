package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.conditions.BuffHasTrait;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.BuffRemovalResist;
import yome.fgo.simulator.models.effects.buffs.Charm;
import yome.fgo.simulator.models.effects.buffs.SureHit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.ATTACKER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.SimulationTest.KAMA_ID;
import static yome.fgo.simulator.models.SimulationTest.KAMA_OPTION;

public class RemoveBuffTest {
    public static final Buff CHARM = BuffFactory.buildBuff(
            BuffData.newBuilder()
                    .setType(Charm.class.getSimpleName())
                    .build(),
            1
    );
    public static final Buff SURE_HIT = BuffFactory.buildBuff(
            BuffData.newBuilder()
                    .setType(SureHit.class.getSimpleName())
                    .build(),
            1
    );
    public static final Buff ATTACK_BUFF = BuffFactory.buildBuff(
            BuffData.newBuilder()
                    .setType(AttackBuff.class.getSimpleName())
                    .addValues(0.3)
                    .build(),
            1
    );

    @Test
    public void testRemoveBuff_removeAny() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .target(SELF)
                .build();

        servant.addBuff(CHARM);
        servant.addBuff(SURE_HIT);
        servant.addBuff(ATTACK_BUFF);

        assertEquals(3, servant.getBuffs().size());

        simulation.setActivator(servant);
        removeBuff.apply(simulation);

        assertEquals(0, servant.getBuffs().size());
    }

    @Test
    public void testRemoveBuff_removeBuff() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .applyCondition(new BuffHasTrait(POSITIVE_BUFF.name()))
                .target(SELF)
                .build();

        servant.addBuff(CHARM);
        servant.addBuff(SURE_HIT);
        servant.addBuff(ATTACK_BUFF);

        assertEquals(3, servant.getBuffs().size());

        simulation.setActivator(servant);
        removeBuff.apply(simulation);

        assertEquals(1, servant.getBuffs().size());
    }

    @Test
    public void testRemoveBuff_removeDebuff() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .applyCondition(new BuffHasTrait(NEGATIVE_BUFF.name()))
                .target(SELF)
                .build();

        servant.addBuff(CHARM);
        servant.addBuff(SURE_HIT);
        servant.addBuff(ATTACK_BUFF);

        assertEquals(3, servant.getBuffs().size());

        simulation.setActivator(servant);
        removeBuff.apply(simulation);

        assertEquals(2, servant.getBuffs().size());
    }

    @Test
    public void testRemoveBuff_removeBuffWithProbabilityThreshold() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));
        simulation.setProbabilityThreshold(1.0);

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .applyCondition(new BuffHasTrait(POSITIVE_BUFF.name()))
                .target(SELF)
                .build();

        servant.addBuff(CHARM);
        servant.addBuff(SURE_HIT);
        servant.addBuff(ATTACK_BUFF);
        servant.addBuff(ATTACK_BUFF);
        servant.addBuff(
                BuffRemovalResist.builder()
                        .value(1.0)
                        .numTimesActive(1)
                        .buffTraits(ImmutableList.of(POSITIVE_BUFF.name()))
                        .build()
        );

        assertEquals(5, servant.getBuffs().size());

        simulation.setActivator(servant);
        removeBuff.apply(simulation);

        assertEquals(4, servant.getBuffs().size());
        assertTrue(servant.isImmobilized());
    }

    @Test
    public void testRemoveBuff_removeDebuffWithBuffRemovalResist() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));
        simulation.setProbabilityThreshold(1.0);

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .applyCondition(new BuffHasTrait(NEGATIVE_BUFF.name()))
                .target(SELF)
                .build();

        servant.addBuff(CHARM);
        servant.addBuff(SURE_HIT);
        servant.addBuff(ATTACK_BUFF);
        servant.addBuff(
                BuffRemovalResist.builder()
                        .value(1.0)
                        .numTimesActive(1)
                        .condition(new BuffHasTrait(POSITIVE_BUFF.name()))
                        .buffTraits(ImmutableList.of(POSITIVE_BUFF.name()))
                        .build()
        );

        assertEquals(4, servant.getBuffs().size());

        simulation.setActivator(servant);
        removeBuff.apply(simulation);

        assertEquals(3, servant.getBuffs().size());
        assertFalse(servant.isImmobilized());
    }

    @Test
    public void testRemoveBuff_irremovable() {
        final Servant kama = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(kama));
        simulation.setProbabilityThreshold(0);

        kama.initiate(simulation);

        assertEquals(12, kama.getBuffs().size());
        kama.activateActiveSkill(simulation, 2);
        assertEquals(14, kama.getBuffs().size());

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .target(SELF)
                .applyCondition(new BuffHasTrait(ATTACKER_BUFF.name()))
                .build();

        simulation.setActivator(kama);
        removeBuff.apply(simulation);

        assertEquals(12, kama.getBuffs().size());
    }
}
