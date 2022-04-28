package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.BuffRemovalResist;
import yome.fgo.simulator.models.effects.buffs.Charm;
import yome.fgo.simulator.models.effects.buffs.SureHit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.SimulationTest.KAMA_ID;
import static yome.fgo.simulator.models.SimulationTest.KAMA_OPTION;
import static yome.fgo.simulator.models.conditions.BuffIsBuff.BUFF_IS_BUFF;
import static yome.fgo.simulator.models.conditions.BuffIsDebuff.BUFF_IS_DEBUFF;

public class RemoveBuffTest {
    @Test
    public void testRemoveBuff_removeAny() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .target(SELF)
                .build();

        servant.addBuff(Charm.builder().build());
        servant.addBuff(SureHit.builder().build());
        servant.addBuff(AttackBuff.builder().value(0.3).build());

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
                .applyCondition(BUFF_IS_BUFF)
                .target(SELF)
                .build();

        servant.addBuff(Charm.builder().build());
        servant.addBuff(SureHit.builder().build());
        servant.addBuff(AttackBuff.builder().value(0.3).build());

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
                .applyCondition(BUFF_IS_DEBUFF)
                .target(SELF)
                .build();

        servant.addBuff(Charm.builder().build());
        servant.addBuff(SureHit.builder().build());
        servant.addBuff(AttackBuff.builder().value(0.3).build());

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
                .applyCondition(BUFF_IS_BUFF)
                .target(SELF)
                .build();

        servant.addBuff(Charm.builder().build());
        servant.addBuff(SureHit.builder().build());
        servant.addBuff(AttackBuff.builder().value(0.3).build());
        servant.addBuff(BuffRemovalResist.builder().value(1.0).numTimesActive(1).build());

        assertEquals(4, servant.getBuffs().size());

        simulation.setActivator(servant);
        removeBuff.apply(simulation);

        assertEquals(3, servant.getBuffs().size());
        assertTrue(servant.isImmobilized());
    }

    @Test
    public void testRemoveBuff_removeDebuffWithBuffRemovalResist() {
        final Servant servant = new Servant();
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(servant));
        simulation.setProbabilityThreshold(1.0);

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .applyCondition(BUFF_IS_DEBUFF)
                .target(SELF)
                .build();

        servant.addBuff(Charm.builder().build());
        servant.addBuff(SureHit.builder().build());
        servant.addBuff(AttackBuff.builder().value(0.3).build());
        servant.addBuff(
                BuffRemovalResist.builder()
                        .value(1.0)
                        .numTimesActive(1)
                        .condition(BUFF_IS_BUFF)
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

        final RemoveBuff removeBuff = RemoveBuff.builder()
                .target(SELF)
                .build();

        simulation.setActivator(kama);
        removeBuff.apply(simulation);

        assertEquals(12, kama.getBuffs().size());
    }
}
