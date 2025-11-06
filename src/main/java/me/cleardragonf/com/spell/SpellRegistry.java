package me.cleardragonf.com.spell;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpellRegistry {
    private static final Map<String, Spell> SPELLS = new LinkedHashMap<>();

    public static Spell register(Spell spell) {
        SPELLS.put(spell.id(), spell);
        return spell;
    }

    public static Spell get(String id) {
        return SPELLS.get(id);
    }

    public static Collection<Spell> all() {
        return Collections.unmodifiableCollection(SPELLS.values());
    }
}

