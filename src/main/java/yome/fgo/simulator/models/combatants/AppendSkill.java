package yome.fgo.simulator.models.combatants;

import yome.fgo.data.proto.FgoStorageData.AppendSkillData;

public class AppendSkill extends Skill {
    public AppendSkill(final AppendSkillData appendSkillData, final int appendSkillLevel) {
        super(appendSkillData.getEffectsList(), appendSkillLevel);
    }
}
