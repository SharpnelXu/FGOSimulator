package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderChangeTest {
    @Test
    public void testOrderChange() {
        final Simulation simulation = new Simulation();
        final Servant onField = mock(Servant.class);
        final Servant inBackup = mock(Servant.class);
        simulation.setCurrentServants(Lists.newArrayList(onField));
        simulation.setBackupServants(Lists.newLinkedList(ImmutableList.of(inBackup)));
        simulation.setOrderChangeSelections(ImmutableList.of(0, 0));

        final OrderChange orderChange = OrderChange.builder().build();

        inBackup.enterField(simulation);
        onField.leaveField(simulation);

        replay(onField, inBackup);

        orderChange.apply(simulation);
        assertEquals(inBackup, simulation.getCurrentServants().get(0));
        assertEquals(onField, simulation.getBackupServants().get(0));

        verify(onField, inBackup);
    }
}
