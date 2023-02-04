package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HEAL_EFFECTIVENESS_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HEAL_GRANT_EFF_BUFF;

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

    @Test
    public void testHpChange_withEff() {
        final Combatant combatant = new Combatant("id", ImmutableList.of(5000));

        assertEquals(5000, combatant.getCurrentHp());
        assertEquals(5000, combatant.getMaxHp());

        combatant.receiveDamage(4000);
        assertEquals(1000, combatant.getCurrentHp());

        final HpChange hpChange = HpChange.builder()
                .target(SELF)
                .values(ImmutableList.of(1000))
                .build();
        final Simulation simulation = new Simulation();
        simulation.setActivator(combatant);

        combatant.addBuff(Buff.builder().buffType(HEAL_EFFECTIVENESS_BUFF).value(0.5).build());

        hpChange.apply(simulation);
        assertEquals(2500, combatant.getCurrentHp());

        combatant.addBuff(Buff.builder().buffType(HEAL_GRANT_EFF_BUFF).value(0.5).build());

        hpChange.apply(simulation);
        assertEquals(4750, combatant.getCurrentHp());
    }
}
