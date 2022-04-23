package yome.fgo.simulator.models.combatants;

public class CombatAction {
    public final int servantIndex;
    public final int commandCardIndex;
    public final boolean isCriticalStrike;
    public final boolean isNoblePhantasm;

    CombatAction(final int servantIndex, final int commandCardIndex, final boolean isCriticalStrike) {
        this.servantIndex = servantIndex;
        this.commandCardIndex = commandCardIndex;
        this.isCriticalStrike = isCriticalStrike;
        this.isNoblePhantasm = false;
    }

    CombatAction(final int servantIndex) {
        this.servantIndex = servantIndex;
        this.commandCardIndex = 0;
        this.isCriticalStrike = false;
        this.isNoblePhantasm = true;
    }

    public static CombatAction createCommandCardAction(
            final int servantIndex, final int commandCardIndex, final boolean isCriticalStrike
    ) {
        return new CombatAction(servantIndex, commandCardIndex, isCriticalStrike);
    }

    public static CombatAction createNoblePhantasmAction(final int servantIndex) {
        return new CombatAction(servantIndex);
    }
}
