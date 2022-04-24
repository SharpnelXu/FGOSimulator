package yome.fgo.simulator.models.combatants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_1;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_2;
import static yome.fgo.simulator.models.combatants.Servant.NP_CAP_3;

public class ServantTest {
    @Test
    public void testCalculateOverchargeLevel() {
        assertEquals(1, Servant.calculateOverchargeLevel(0, 50));
        assertEquals(1, Servant.calculateOverchargeLevel(0, 99.99));
        assertEquals(1, Servant.calculateOverchargeLevel(0, NP_CAP_1));
        assertEquals(2, Servant.calculateOverchargeLevel(1, 100.2));
        assertEquals(3, Servant.calculateOverchargeLevel(0, NP_CAP_3));
        assertEquals(4, Servant.calculateOverchargeLevel(2, NP_CAP_2));
        assertEquals(4, Servant.calculateOverchargeLevel(2, 212.35));
        assertEquals(4, Servant.calculateOverchargeLevel(2, 299.99));
        assertEquals(5, Servant.calculateOverchargeLevel(2, NP_CAP_3));
        assertEquals(5, Servant.calculateOverchargeLevel(20, 2000.2));
    }
}
