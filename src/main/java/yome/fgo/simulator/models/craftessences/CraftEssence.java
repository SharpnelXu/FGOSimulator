package yome.fgo.simulator.models.craftessences;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.simulator.models.combatants.Skill;

@Getter
public class CraftEssence extends Skill {
    private final int attack;
    private final int hp;

    public CraftEssence(final CraftEssenceData craftEssenceData, final CraftEssenceOption craftEssenceOption) {
        super(craftEssenceData.getEffectsList(), craftEssenceOption.getIsLimitBreak() ? 2 : 1);
        final int ceLevel = craftEssenceOption.getCraftEssenceLevel();
        this.hp = craftEssenceData.getStatusDataList().get(ceLevel - 1).getHP();
        this.attack = craftEssenceData.getStatusDataList().get(ceLevel - 1).getATK();
    }
}
