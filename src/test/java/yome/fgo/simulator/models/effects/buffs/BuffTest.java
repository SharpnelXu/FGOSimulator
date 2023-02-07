package yome.fgo.simulator.models.effects.buffs;

import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.variations.TurnPassVariation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.effects.buffs.BuffType.ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DELAYED_EFFECT;
import static yome.fgo.simulator.models.variations.TurnPassVariation.TURN_PASS_VARIATION;

public class BuffTest {
    @Test
    public void testCommonMethods() {
        final Buff buff = Buff.builder().build();
        final List<String> buffTraits = buff.buffTraits;

        assertFalse(buff.isBuff());
        buffTraits.add(POSITIVE_BUFF.name());
        assertTrue(buff.isBuff());

        assertFalse(buff.isDebuff());
        buffTraits.add(NEGATIVE_BUFF.name());
        assertTrue(buff.isDebuff());
    }

    @Test
    public void testShouldApply_belowThreshold() {
        final Buff buff = Buff.builder().condition(ALWAYS).probability(0.3).build();
        final Simulation simulation = new Simulation();
        simulation.setProbabilityThreshold(0.5);
        assertFalse(buff.shouldApply(simulation));
        simulation.setProbabilityThreshold(0.2);
        assertTrue(buff.shouldApply(simulation));
    }

    @Test
    public void testShouldApply_delayedEffect() {
        final Buff buff = Buff.builder().buffType(DELAYED_EFFECT).condition(ALWAYS).activeTurns(3).build();
        final Simulation simulation = new Simulation();
        simulation.setProbabilityThreshold(1.0);
        assertFalse(buff.shouldApply(simulation));
        buff.decrementActiveTurns();
        assertFalse(buff.shouldApply(simulation));
        buff.decrementActiveTurns();
        assertTrue(buff.shouldApply(simulation));
        buff.decrementActiveTurns();
        assertFalse(buff.shouldApply(simulation));
    }

    @Test
    public void testExecutionMethods() {
        final Buff buff = Buff.builder().activeTurns(3).activeTimes(3).build();
        assertFalse(buff.isApplied());
        assertEquals(0, buff.getTurnPassed());
        assertEquals(3, buff.getActiveTurns());
        assertEquals(3, buff.getActiveTimes());
        assertFalse(buff.isPermanentEffect());

        buff.setApplied();
        assertTrue(buff.isApplied());

        buff.decrementActiveTurns();
        assertTrue(buff.isApplied());
        assertEquals(1, buff.getTurnPassed());
        assertEquals(2, buff.getActiveTurns());

        buff.decrementActiveTimes();
        assertFalse(buff.isApplied());
        assertEquals(2, buff.getActiveTimes());
    }

    @Test
    public void testGetValue() {
        final Buff buff = Buff.builder()
                .buffType(ATTACK_BUFF)
                .value(0.5)
                .variation(TURN_PASS_VARIATION)
                .addition(0.2)
                .build();
        final Simulation simulation = new Simulation();
        assertEquals(0.5, buff.getValue(simulation), 0.00001);

        buff.decrementActiveTurns();
        assertEquals(0.7, buff.getValue(simulation), 0.00000);

        buff.decrementActiveTurns();
        assertEquals(0.9, buff.getValue(simulation), 0.00001);

        buff.addEffectiveness(0.5);
        assertEquals(1.35, buff.getValue(simulation), 0.00001);

        buff.decrementActiveTurns();
        assertEquals(1.65, buff.getValue(simulation), 0.00001);

        buff.addEffectiveness(0.5);
        assertEquals(2.2, buff.getValue(simulation), 0.00001);
    }
}
