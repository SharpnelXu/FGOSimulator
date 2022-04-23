package yome.fgo.simulator.models.combatants;

import yome.fgo.simulator.models.effects.Effect;

import java.util.ArrayList;
import java.util.List;

public class AppendSkill {
    public String name;
    public int level;
    public List<Effect> effects;

    protected AppendSkill(String name, int level, List<Effect> effects) {
        this.name = name;
        this.level = level;
        this.effects = effects;
    }

    public static class Builder {
        private String name;
        private int level = 1;
        private final List<Effect> effects = new ArrayList<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
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

        public AppendSkill build() {
            return new AppendSkill(name, level, effects);
        }
    }
}
