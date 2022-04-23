package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;

@Getter
public class ActiveSkill extends Skill {
    private final int maxCoolDown;
    private int currentCoolDown;

    public ActiveSkill(final ActiveSkillData activeSkillData, final int activeSkillLevel) {
        super(activeSkillData.getEffectsList(), activeSkillLevel);
        this.maxCoolDown = activeSkillData.getBaseCoolDown() - activeSkillLevel / 5;
    }
}
