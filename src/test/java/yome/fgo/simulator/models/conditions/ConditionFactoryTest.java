package yome.fgo.simulator.models.conditions;

import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.ConditionData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;

public class ConditionFactoryTest {
    @Test
    public void testBuildCondition_always() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType(Always.class.getSimpleName())
                .build();

        assertEquals(ALWAYS, buildCondition(conditionData));
    }
    @Test
    public void testBuildCondition_unsupported() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType("RandomClassName")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> buildCondition(conditionData));
    }
}
