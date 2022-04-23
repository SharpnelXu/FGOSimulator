package yome.fgo.simulator.models.combatants;

import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.simulator.models.effects.Effect;

import java.util.List;

public class PassiveSkill extends Skill {
    public List<Effect> effects;

    public PassiveSkill(final PassiveSkillData passiveSkillData) {
        super(passiveSkillData.getEffectsList(), 1);
    }
}
