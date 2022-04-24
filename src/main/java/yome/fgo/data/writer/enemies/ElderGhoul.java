package yome.fgo.data.writer.enemies;

import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.simulator.translation.Traits;

import static yome.fgo.data.proto.FgoStorageData.Attribute.EARTH;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ASSASSIN;
import static yome.fgo.data.writer.DataWriterMain.writeEnemy;
import static yome.fgo.simulator.translation.EnemyCategory.DECEASED;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.GHOUL;

public class ElderGhoul {
    public static void main(final String[] args) {
        final String id = "elderGhoul";
        final CombatantData combatantData = CombatantData.newBuilder()
                .setId(id)
                .setRarity(4)
                .setFateClass(ASSASSIN)
                .setAttribute(EARTH)
                .addTraits(Traits.DEMONIC)
                .addTraits(Traits.UNDEAD)
                .addTraits(Traits.ONI)
                .setDeathRate(0.2)
                .build();

        writeEnemy(combatantData, DECEASED, GHOUL);
    }
}
