package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;

public class HpChangeTest {
    @Test
    public void testHpChange() {
        final Combatant combatant = new Combatant("id", ImmutableList.of(5000));

        assertEquals(5000, combatant.getCurrentHp());
        assertEquals(5000, combatant.getMaxHp());

        combatant.receiveDamage(2500);
        assertEquals(2500, combatant.getCurrentHp());

        final HpChange hpChange = HpChange.builder()
                .target(SELF)
                .values(ImmutableList.of(1300))
                .build();
        final Simulation simulation = new Simulation();
        simulation.setActivator(combatant);

        hpChange.apply(simulation);
        assertEquals(3800, combatant.getCurrentHp());
        hpChange.apply(simulation);
        assertEquals(5000, combatant.getCurrentHp());
    }
}
