package yome.fgo.simulator.models.conditions;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES;
import static yome.fgo.data.proto.FgoStorageData.Target.EFFECT_TARGET;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;
import static yome.fgo.simulator.models.traits.Traits.DEMONIC;
import static yome.fgo.simulator.models.traits.Traits.RIDING;

public class TargetsHaveTraitTest {
    @Test
    public void testTargetsHaveTrait_single() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType(TargetsHaveTrait.class.getSimpleName())
                .setTarget(EFFECT_TARGET)
                .setValue(RIDING)
                .build();

        final Condition condition = buildCondition(conditionData);
        final Simulation simulation = new Simulation();

        final CombatantData riding = CombatantData.newBuilder()
                .addTraits(RIDING)
                .build();
        simulation.setEffectTarget(new Servant("", riding));
        assertTrue(condition.evaluate(simulation));

        final CombatantData nonRiding = CombatantData.newBuilder()
                .addTraits(DEMONIC)
                .build();
        simulation.setEffectTarget(new Servant("", nonRiding));
        assertFalse(condition.evaluate(simulation));
    }
    @Test
    public void testTargetsHaveTrait_multiple() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType(TargetsHaveTrait.class.getSimpleName())
                .setTarget(ALL_ALLIES)
                .setValue(RIDING)
                .build();

        final Condition condition = buildCondition(conditionData);
        final Simulation simulation = new Simulation();

        final CombatantData riding = CombatantData.newBuilder()
                .addTraits(RIDING)
                .build();
        final CombatantData nonRiding = CombatantData.newBuilder()
                .addTraits(DEMONIC)
                .build();
        simulation.setCurrentServants(ImmutableList.of(
                new Servant("riding", riding),
                new Servant("nonRiding", nonRiding)
        ));
        assertFalse(condition.evaluate(simulation));
    }
}
