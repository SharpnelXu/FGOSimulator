package yome.fgo.simulator.models.combatants;

import yome.fgo.simulator.models.effects.Effect;

import java.util.ArrayList;
import java.util.List;

public class PassiveSkill {
    public String name;
    public List<Effect> effects;

    protected PassiveSkill(String name, List<Effect> effects) {
        this.name = name;
        this.effects = effects;
    }

    public static class Builder {
        private String name;
        private final List<Effect> effects = new ArrayList<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder addBuff(Effect effect) {
            this.effects.add(effect);
            return this;
        }

        public Builder clearBuffs() {
            this.effects.clear();
            return this;
        }

        public PassiveSkill build() {
            return new PassiveSkill(this.name, this.effects);
        }
    }
}
