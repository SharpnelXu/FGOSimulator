package yome.fgo.simulator.models.effects;

import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.effects.NoblePhantasmDamage.NpDamageParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.FateClass.AVENGER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.simulator.models.effects.NoblePhantasmDamage.calculateTotalNpDamage;

public class NoblePhantasmDamageTest {

    @Test
    public void testCalculateTotalNpDamage_kamaAvenger() {
        final NpDamageParameters.NpDamageParametersBuilder npDamageParamsBuilder = NpDamageParameters.builder()
                .attack(20977)
                .totalHits(100)
                .damageRate(7.5)
                .attackerClass(AVENGER)
                .attackerAttribute(SKY)
                .currentCardType(ARTS)
                .damageAdditionBuff(225)
                .fixedRandom(0.9);

        // Kama (Avenger) NP
        npDamageParamsBuilder.defenderClass(CASTER).defenderAttribute(SKY);
        npDamageParamsBuilder.npSpecificAttackRate(1.0);
        assertEquals(36046.0, calculateTotalNpDamage(npDamageParamsBuilder.build()), 10);

        // Kama (Avenger) NP, with all skills
        npDamageParamsBuilder.commandCardBuff(0.3)
                .npDamageBuff(0.2)
                .npSpecificAttackRate(1.5);
        assertEquals(84049.0, calculateTotalNpDamage(npDamageParamsBuilder.build()), 10);
    }
}
