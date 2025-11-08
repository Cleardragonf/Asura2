package me.cleardragonf.com.ward;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WardSpec {
    private final String name; // display/name key (we map by this)
    private final List<OptionSpec> options;

    public WardSpec(String name, List<OptionSpec> options) {
        this.name = name;
        this.options = List.copyOf(options);
    }

    public String name() { return name; }
    public List<OptionSpec> options() { return Collections.unmodifiableList(options); }

    public CompoundTag defaultOptionsTag() {
        CompoundTag tag = new CompoundTag();
        for (OptionSpec opt : options) {
            if (opt instanceof BoolOption b) {
                tag.putBoolean(b.key(), b.defaultValue());
            }
        }
        return tag;
    }
}

