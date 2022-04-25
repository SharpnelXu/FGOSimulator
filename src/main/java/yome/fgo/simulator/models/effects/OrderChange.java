package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

@SuperBuilder
public class OrderChange extends Effect {
    public static final OrderChange ORDER_CHANGE = OrderChange.builder().build();

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final Servant servantOnField = simulation.getTargetedAlly();
        final Servant servantInBackup = simulation.getTargetedBackup();
        final int onFieldIndex = simulation.getCurrentAllyTargetIndex();
        final int backupIndex = simulation.getCurrentBackupTargetIndex();

        simulation.getBackupServants().set(backupIndex, servantOnField);
        simulation.getCurrentServants().set(onFieldIndex, servantInBackup);

        servantInBackup.enterField(simulation);
    }
}
