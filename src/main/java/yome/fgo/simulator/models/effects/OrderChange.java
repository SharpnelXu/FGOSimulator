package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import java.util.List;

@SuperBuilder
public class OrderChange extends Effect {
    public static final OrderChange ORDER_CHANGE = OrderChange.builder().build();

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final List<Servant> targets = simulation.getOrderChangeTargets();
        final Servant servantOnField = targets.get(0);
        final Servant servantInBackup = targets.get(1);
        final int onFieldIndex = simulation.getCurrentAllyTargetIndex();
        final int backupIndex = simulation.getCurrentBackupTargetIndex();

        simulation.getBackupServants().set(backupIndex, servantOnField);
        simulation.getCurrentServants().set(onFieldIndex, servantInBackup);

        servantInBackup.enterField(simulation);
    }
}
