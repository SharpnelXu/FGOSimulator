package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_FIXED_RATE;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_REMOVE_ADV;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_REMOVE_DISADV;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_L;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_R;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CLASS_ADVANTAGE_CHANGE_BUFF;

public class ClassAdvantageBuffTest {
    @Test
    public void testClassAdvantageBuff_noChange() {
        final Buff buff = Buff.builder()
                .buffType(CLASS_ADVANTAGE_CHANGE_BUFF)
                .attackMode(CLASS_ADV_NO_CHANGE)
                .attackAdvantage(3.0)
                .attackModeAffectedClasses(Lists.newArrayList(BEAST_III_L))
                .build();

        assertEquals(2.0, buff.asAttacker(2.0, BEAST_III_L));
        assertEquals(2.0, buff.asDefender(2.0, BEAST_III_L));
    }

    @Test
    public void testClassAdvantageBuff_notAffected() {
        final Buff buff = Buff.builder()
                .buffType(CLASS_ADVANTAGE_CHANGE_BUFF)
                .attackMode(CLASS_ADV_FIXED_RATE)
                .attackAdvantage(3.0)
                .attackModeAffectedClasses(Lists.newArrayList(BEAST_III_L))
                .build();

        assertEquals(2.0, buff.asAttacker(2.0, BEAST_III_R));
        assertEquals(3.0, buff.asAttacker(2.0, BEAST_III_L));
        assertEquals(2.0, buff.asDefender(2.0, BEAST_III_L));
    }

    @Test
    public void testClassAdvantageBuff_removeDisAdvantage() {
        final Buff buff = Buff.builder()
                .buffType(CLASS_ADVANTAGE_CHANGE_BUFF)
                .defenseMode(CLASS_ADV_REMOVE_DISADV)
                .defenseModeAffectedClasses(Lists.newArrayList(BEAST_III_L))
                .build();

        assertEquals(2.0, buff.asAttacker(2.0, BEAST_III_L));
        assertEquals(1.0, buff.asDefender(2.0, BEAST_III_L));
    }

    @Test
    public void testClassAdvantageBuff_removeAdvantage() {
        final Buff buff = Buff.builder()
                .buffType(CLASS_ADVANTAGE_CHANGE_BUFF)
                .defenseMode(CLASS_ADV_REMOVE_ADV)
                .defenseModeAffectedClasses(Lists.newArrayList(BEAST_III_L))
                .build();

        assertEquals(2.0, buff.asAttacker(2.0, BEAST_III_L));
        assertEquals(1.0, buff.asDefender(0.5, BEAST_III_L));
    }
}
