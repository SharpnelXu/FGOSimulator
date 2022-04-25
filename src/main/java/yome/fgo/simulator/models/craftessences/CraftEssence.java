package yome.fgo.simulator.models.craftessences;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.Status;
import yome.fgo.simulator.models.combatants.Skill;

@Getter
public class CraftEssence extends Skill {
    private final int attack;
    private final int hp;

    public CraftEssence(final CraftEssenceData craftEssenceData, final CraftEssenceOption craftEssenceOption) {
        super(craftEssenceData.getEffectsList(), craftEssenceOption.getIsLimitBreak() ? 2 : 1);
        final int ceLevel = craftEssenceOption.getCraftEssenceLevel();
        this.hp = craftEssenceData.getHpStats(ceLevel - 1);
        this.attack = craftEssenceData.getAtkStats(ceLevel - 1);
    }
}
