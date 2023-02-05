package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.Traits;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_1;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_2;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_3;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.OVERCHARGE_BUFF;

public class ServantTest {
    @Test
    public void testCalculateOverchargeLevel() {
        final Servant servant = new Servant();
        servant.setNoblePhantasmLevel(5);
        final Simulation simulation = new Simulation();
        servant.changeNp(0.5);
        assertEquals(1, servant.calculateOverchargeLevel(simulation, 0));
        servant.changeNp(-10);
        servant.changeNp(0.9899);
        assertEquals(1, servant.calculateOverchargeLevel(simulation, 0));
        servant.changeNp(-10);
        servant.changeNp(NP_CAP_1);
        assertEquals(1, servant.calculateOverchargeLevel(simulation, 0));
        servant.changeNp(-10);
        servant.changeNp(1.002);
        assertEquals(2, servant.calculateOverchargeLevel(simulation, 1));
        servant.changeNp(-10);
        servant.changeNp(NP_CAP_3);
        assertEquals(3, servant.calculateOverchargeLevel(simulation, 0));
        servant.changeNp(-10);
        servant.changeNp(NP_CAP_2);
        assertEquals(4, servant.calculateOverchargeLevel(simulation, 2));
        servant.changeNp(-10);
        servant.changeNp(2.1235);
        assertEquals(4, servant.calculateOverchargeLevel(simulation, 2));
        servant.changeNp(-10);
        servant.changeNp(2.9999);
        assertEquals(4, servant.calculateOverchargeLevel(simulation, 2));
        servant.changeNp(-10);
        servant.changeNp(NP_CAP_3);
        assertEquals(5, servant.calculateOverchargeLevel(simulation, 2));
        servant.changeNp(-10);
        servant.changeNp(20.002);
        assertEquals(5, servant.calculateOverchargeLevel(simulation, 20));
        servant.changeNp(-10);
        servant.addBuff(Buff.builder().buffType(OVERCHARGE_BUFF).value(2).build());
        servant.changeNp(1);
        assertEquals(4, servant.calculateOverchargeLevel(simulation, 1));
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

        simulation.setCurrentCommandCard(kama.getCommandCard(2));

        final double artsBuff = kama.applyValuedBuff(simulation, COMMAND_CARD_BUFF);
        assertEquals(0.3, artsBuff);
    }
}
