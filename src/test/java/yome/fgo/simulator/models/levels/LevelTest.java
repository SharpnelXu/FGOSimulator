package yome.fgo.simulator.models.levels;

import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.models.combatants.Combatant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Attribute.EARTH;
import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.simulator.translation.EnemyCategory.DECEASED;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.DEMON;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.GHOUL;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.MINION;
import static yome.fgo.simulator.translation.EnemyCategory.OTHER;
import static yome.fgo.simulator.translation.EnemyCategory.TRANSENDENT;

public class LevelTest {
    @Test
    public void testConstructor() {
        final StageData stageData1 = StageData.newBuilder()
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(35219)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("elderGhoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(40150)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(35611)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .build();
        final StageData stageData2 = StageData.newBuilder()
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(39276)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(39705)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("elderGhoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(44162)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .build();
        final StageData stageData3 = StageData.newBuilder()
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("alraune")
                                      .setEnemyCategories(TRANSENDENT + "/" + DEMON)
                                      .addHpBars(200454)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(75249)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("magicalStick")
                                      .setEnemyCategories(OTHER + "/" + MINION)
                                      .addHpBars(75777))
                .build();
        final LevelData levelData = LevelData.newBuilder()
                .setId("test")
                .addStageData(stageData1)
                .addStageData(stageData2)
                .addStageData(stageData3)
                .build();

        final Level level = new Level(levelData);
        assertTrue(level.hasNextStage(1));
        assertFalse(level.hasNextStage(3));
        final Stage stage3 = level.getStage(3);


        final Combatant enemy1 = stage3.getNextEnemy();
        assertEquals("alraune", enemy1.getId());
        assertEquals(200454, enemy1.getCurrentHp());
        assertEquals(EARTH, enemy1.getAttribute());
        assertEquals(RIDER, enemy1.getFateClass());
        final Combatant enemy2 = stage3.getNextEnemy();
        assertEquals("ghoul", enemy2.getId());
        assertEquals(75249, enemy2.getCurrentHp());
        assertEquals(RIDER, enemy2.getFateClass());
        final Combatant enemy3 = stage3.getNextEnemy();
        assertEquals("magicalStick", enemy3.getId());
        assertEquals(75777, enemy3.getCurrentHp());
        assertEquals(SKY, enemy3.getAttribute());
        assertEquals(BERSERKER, enemy3.getFateClass());
    }
}
