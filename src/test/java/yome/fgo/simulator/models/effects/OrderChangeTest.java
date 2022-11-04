package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.gui.components.SimulationWindow;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget.ORDER_CHANGE;

public class OrderChangeTest {
    @Test
    public void testOrderChange() {
        final Simulation simulation = new Simulation();
        final Servant onField = mock(Servant.class);
        final Servant inBackup = mock(Servant.class);
        final SimulationWindow simulationWindow = mock(SimulationWindow.class);
        simulation.setSimulationWindow(simulationWindow);
        simulation.setCurrentServants(Lists.newArrayList(onField));
        simulation.setBackupServants(Lists.newLinkedList(ImmutableList.of(inBackup)));
        simulation.setOrderChangeSelections(ImmutableList.of(0, 0));

        final OrderChange orderChange = OrderChange.builder().build();

        inBackup.enterField(simulation);
        onField.leaveField(simulation);
        simulationWindow.showSpecialTargetSelectionWindow(
                SpecialActivationParams.newBuilder()
                        .setSpecialTarget(ORDER_CHANGE)
                        .build()
        );

        replay(onField, inBackup);

        orderChange.apply(simulation);
        assertEquals(inBackup, simulation.getCurrentServants().get(0));
        assertEquals(onField, simulation.getBackupServants().get(0));

        verify(onField, inBackup);
    }
}
