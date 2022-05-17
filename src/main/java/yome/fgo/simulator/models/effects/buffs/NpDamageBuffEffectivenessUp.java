package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class NpDamageBuffEffectivenessUp extends ValuedBuff implements AttackerBuff {
    @Override
    public boolean commonStackableCondition() {
        return false;
    }
}
