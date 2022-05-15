package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.models.SimulationTest.KAMA_ID;
import static yome.fgo.simulator.models.SimulationTest.KAMA_OPTION;

public class CardTypeChangeTest {
    @Test
    public void testCardTypeChange() {
        final Simulation simulation = new Simulation();
        final Servant kama = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        simulation.setCurrentServants(Lists.newArrayList(kama));

        assertEquals(QUICK, kama.getCommandCard(0).getCommandCardType());
        assertThat(kama.getCommandCard(0).getHitPercentages()).containsExactly(10, 20, 30, 40);
        assertEquals(500, kama.getCommandCard(0).getCommandCardStrengthen());
        assertEquals(QUICK, kama.getCommandCard(1).getCommandCardType());
        assertEquals(ARTS, kama.getCommandCard(2).getCommandCardType());
        assertEquals(ARTS, kama.getCommandCard(3).getCommandCardType());
        assertEquals(BUSTER, kama.getCommandCard(4).getCommandCardType());
        assertThat(kama.getCommandCard(4).getHitPercentages()).containsExactly(16, 33, 51);

        final CardTypeChange cardTypeChange = CardTypeChange.builder()
                .commandCardType(BUSTER)
                .build();

        kama.addBuff(cardTypeChange);

        assertEquals(BUSTER, kama.getCommandCard(0).getCommandCardType());
        assertThat(kama.getCommandCard(0).getHitPercentages()).containsExactly(16, 33, 51);
        assertEquals(500, kama.getCommandCard(0).getCommandCardStrengthen());
        assertEquals(BUSTER, kama.getCommandCard(1).getCommandCardType());
        assertEquals(BUSTER, kama.getCommandCard(2).getCommandCardType());
        assertEquals(BUSTER, kama.getCommandCard(3).getCommandCardType());
        assertEquals(BUSTER, kama.getCommandCard(4).getCommandCardType());
        assertThat(kama.getCommandCard(4).getHitPercentages()).containsExactly(16, 33, 51);
    }
}
