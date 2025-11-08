package me.cleardragonf.com.item;

import me.cleardragonf.com.ward.BoolOption;
import me.cleardragonf.com.ward.WardSpec;

import java.util.List;

public class HealWardItem extends WardSpellItem {
    private static final WardSpec SPEC = new WardSpec(
            "Healing Ward",
            List.of(
                    new BoolOption("this_is_a_test", "This Is A Test", false),
                    new BoolOption("friendly_only", "Affects Allies Only", true)
            )
    );

    public HealWardItem(Properties props) {
        super(props, "Healing Ward", SPEC);
    }
}
