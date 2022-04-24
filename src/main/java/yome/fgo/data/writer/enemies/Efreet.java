package yome.fgo.data.writer.enemies;

import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.simulator.translation.Traits;

import static yome.fgo.data.proto.FgoStorageData.Attribute.EARTH;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.data.proto.FgoStorageData.Gender.FEMALE;
import static yome.fgo.data.writer.DataWriterMain.writeEnemy;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.DEMON;
import static yome.fgo.simulator.translation.EnemyCategory.TRANSENDENT;

public class Efreet {
    public static void main(final String[] args) {
        final String id = "efreet";
        final CombatantData combatantData = CombatantData.newBuilder()
                .setId(id)
                .setRarity(4)
                .setFateClass(CASTER)
                .setGender(FEMALE)
                .setAttribute(EARTH)
                .addTraits(Traits.DEMONIC)
                .setDeathRate(0.1)
                .build();

        writeEnemy(combatantData, TRANSENDENT, DEMON);
    }
}
