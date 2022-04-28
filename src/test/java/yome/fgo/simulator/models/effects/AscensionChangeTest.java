package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.buffs.SureHit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.SimulationTest.KAMA_ID;
import static yome.fgo.simulator.models.SimulationTest.KAMA_OPTION;

public class AscensionChangeTest {
    @Test
    public void testAscensionChange() {
        final Servant kama1 = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        final Servant kama2 = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        final Servant kama3 = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);

        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(Lists.newArrayList(kama1, kama2, kama3));
        final AscensionChange ascensionChange = AscensionChange.builder()
                .target(SELF)
                .ascensionLevels(ImmutableList.of(2))
                .build();

        kama1.initiate(simulation);
        kama2.initiate(simulation);
        kama3.initiate(simulation);

        assertEquals(14, kama1.getBuffs().size());
        assertEquals(14, kama2.getBuffs().size());
        assertEquals(14, kama3.getBuffs().size());
        kama2.activateActiveSkill(simulation, 2);

        assertEquals(6, kama2.getActiveSkills().get(2).getCurrentCoolDown());
        assertEquals(14, kama1.getBuffs().size());
        assertEquals(16, kama2.getBuffs().size());
        assertEquals(14, kama3.getBuffs().size());

        kama2.addBuff(SureHit.builder().activator(kama2).isPassive(true).build());
        assertEquals(17, kama2.getBuffs().size());

        simulation.setActivator(kama2);
        ascensionChange.apply(simulation);

        final NpChange npChange = NpChange.builder()
                .target(SELF)
                .npChanges(ImmutableList.of(1.0))
                .build();
        npChange.apply(simulation);

        assertEquals(1.7, kama2.getCurrentNp());
        assertEquals(6, kama2.getActiveSkills().get(2).getCurrentCoolDown());
        assertEquals(14, kama1.getBuffs().size());
        assertEquals(16, kama2.getBuffs().size());
        assertEquals(14, kama3.getBuffs().size());
    }
}
