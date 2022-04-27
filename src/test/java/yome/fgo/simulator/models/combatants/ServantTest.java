package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.Traits;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_1;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_2;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_3;

public class ServantTest {
    @Test
    public void testCalculateOverchargeLevel() {
        assertEquals(1, Servant.calculateOverchargeLevel(0, 0.50));
        assertEquals(1, Servant.calculateOverchargeLevel(0, 0.9999));
        assertEquals(1, Servant.calculateOverchargeLevel(0, NP_CAP_1));
        assertEquals(2, Servant.calculateOverchargeLevel(1, 1.002));
        assertEquals(3, Servant.calculateOverchargeLevel(0, NP_CAP_3));
        assertEquals(4, Servant.calculateOverchargeLevel(2, NP_CAP_2));
        assertEquals(4, Servant.calculateOverchargeLevel(2, 2.1235));
        assertEquals(4, Servant.calculateOverchargeLevel(2, 2.9999));
        assertEquals(5, Servant.calculateOverchargeLevel(2, NP_CAP_3));
        assertEquals(5, Servant.calculateOverchargeLevel(20, 20.002));
    }

    @Test
    public void testConstructor() {
        final String id = "servant321";
        final ServantOption servantOption = ServantOption.newBuilder()
                .setServantLevel(120)
                .setNoblePhantasmRank(1)
                .setNoblePhantasmLevel(5)
                .setAttackStatusUp(2000)
                .setHealthStatusUp(2000)
                .addAllActiveSkillRanks(ImmutableList.of(1, 1, 1))
                .addAllActiveSkillLevels(ImmutableList.of(10, 10, 10))
                .addAllAppendSkillLevels(ImmutableList.of(10, 10, 10))
                .addCommandCardOptions(CommandCardOption.newBuilder())
                .addCommandCardOptions(CommandCardOption.newBuilder())
                .addCommandCardOptions(CommandCardOption.newBuilder())
                .addCommandCardOptions(CommandCardOption.newBuilder())
                .addCommandCardOptions(CommandCardOption.newBuilder())
                .setBond(15)
                .setAscension(1)
                .build();

        final Servant kama = new Servant(id, ResourceManager.getServantData(id), servantOption);

        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(new ArrayList<>(ImmutableList.of(kama)));
        kama.initiate(simulation);

        final List<String> traits = kama.getAllTraits(simulation);
        assertTrue(traits.contains(Traits.CHILD_SERVANT.name()));

        simulation.setActivator(kama);
        kama.activateActiveSkill(simulation, 0);

        simulation.setCurrentCommandCard(kama.getCommandCard(simulation, 2));

        final double artsBuff = kama.applyBuff(simulation, CommandCardBuff.class);
        assertEquals(0.3, artsBuff);
    }
}
