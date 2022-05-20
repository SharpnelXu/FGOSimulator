package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.conditions.Never;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.simulator.models.effects.EffectFactory.buildEffect;

public class EffectFactoryTest extends EasyMockSupport {
    private Simulation simulation;

    @BeforeEach
    public void init() {
        simulation = mock(Simulation.class);
    }

    @AfterEach
    public void after() {
        verifyAll();
    }

    @Test
    public void testEffectFactory_unsupported() {
        final EffectData effectData = EffectData.newBuilder()
                .setType("Custom")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> buildEffect(effectData, 0));
        replayAll();
    }

    @Test
    public void testEffectFactory_critStar_nonOvercharged() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(CriticalStarChange.class.getSimpleName())
                .addAllIntValues(ImmutableList.of(10, 15, 20))
                .build();

        final Effect effect = buildEffect(effectData, 2);

        expect(simulation.getActivator()).andReturn(new Servant());
        simulation.gainStar(15.0);
        expect(simulation.getStatsLogger()).andReturn(null);
        replayAll();

        effect.apply(simulation);
    }

    @Test
    public void testEffectFactory_critStar_overcharged() {
        final EffectData effectData = EffectData.newBuilder()
                .setType(CriticalStarChange.class.getSimpleName())
                .addAllIntValues(ImmutableList.of(10, 15, 20))
                .setIsOverchargedEffect(true)
                .build();

        final Effect effect = buildEffect(effectData, 2);

        expect(simulation.getActivator()).andReturn(new Servant());
        simulation.gainStar(20.0);
        expect(simulation.getStatsLogger()).andReturn(null);
        replayAll();

        effect.apply(simulation, 3);
    }

    @Test
    public void testEffectFactory_critStar_conditional() {
        final EffectData alwaysData = EffectData.newBuilder()
                .setType(CriticalStarChange.class.getSimpleName())
                .addAllIntValues(ImmutableList.of(10, 15, 20))
                .build();

        final Effect always = buildEffect(alwaysData, 2);
        assertTrue(always.shouldApply(simulation));

        final EffectData neverData = EffectData.newBuilder()
                .setType(CriticalStarChange.class.getSimpleName())
                .addAllIntValues(ImmutableList.of(10, 15, 20))
                .setApplyCondition(ConditionData.newBuilder().setType(Never.class.getSimpleName()))
                .build();

        final Effect never = buildEffect(neverData, 2);
        assertFalse(never.shouldApply(simulation));

        replayAll();
    }
}
