package yome.fgo.data.writer.levels;

import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.StageData;

import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.data.writer.DataWriter.writeLevel;
import static yome.fgo.simulator.translation.EnemyCategory.DECEASED;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.DEMON;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.GHOUL;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.MINION;
import static yome.fgo.simulator.translation.EnemyCategory.OTHER;
import static yome.fgo.simulator.translation.EnemyCategory.TRANSENDENT;

public class HQ11TestLevel {
    public static void main(final String[] args) {
        final String id = "hq11_day5_90+_2";
        final String subPath = "events/hq11";

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
                .setId(id)
                .addStageData(stageData1)
                .addStageData(stageData2)
                .addStageData(stageData3)
                .build();

        writeLevel(levelData, subPath);
    }
}
