package yome.fgo.simulator.models.combatants;

import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;

public class PassiveSkill extends Skill {
    public PassiveSkill(final PassiveSkillData passiveSkillData) {
        super(passiveSkillData.getEffectsList(), 1);
    }
}
