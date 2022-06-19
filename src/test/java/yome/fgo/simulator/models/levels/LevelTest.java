package yome.fgo.simulator.models.levels;

import org.junit.jupiter.api.Test;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.combatants.Combatant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Attribute.EARTH;
import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;

public class LevelTest {
    public static final String TEST_LEVEL_NAME = "【05】【90】爱娜温·狩猎 典位级";
    public static final String TEST_LEVEL_PATH = "活动/2022/【08】狩猎关卡 第11弹";

    @Test
    public void testConstructor() {
        final Level level = new Level(ResourceManager.getLevelData(TEST_LEVEL_PATH, TEST_LEVEL_NAME));
        assertTrue(level.hasNextStage(1));
        assertFalse(level.hasNextStage(3));
        final Stage stage3 = level.getStage(3);


        final Combatant enemy1 = stage3.getNextEnemy();
        assertEquals("爱娜温", enemy1.getId());
        assertEquals(200454, enemy1.getCurrentHp());
        assertEquals(EARTH, enemy1.getAttribute());
        assertEquals(RIDER, enemy1.getFateClass());
        final Combatant enemy2 = stage3.getNextEnemy();
        assertEquals("食尸鬼", enemy2.getId());
        assertEquals(75249, enemy2.getCurrentHp());
        assertEquals(RIDER, enemy2.getFateClass());
        final Combatant enemy3 = stage3.getNextEnemy();
        assertEquals("魔法杖", enemy3.getId());
        assertEquals(75777, enemy3.getCurrentHp());
        assertEquals(SKY, enemy3.getAttribute());
        assertEquals(BERSERKER, enemy3.getFateClass());
    }
}
