package me.cleardragonf.com.spell;

import me.cleardragonf.com.spell.impl.HealPulseSpell;
import me.cleardragonf.com.spell.impl.WaterBoltSpell;

public class Spells {
    private static boolean bootstrapped = false;
    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        SpellRegistry.register(new WaterBoltSpell());
        SpellRegistry.register(new HealPulseSpell());
    }
}

