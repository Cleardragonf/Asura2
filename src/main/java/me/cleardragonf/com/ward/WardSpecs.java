package me.cleardragonf.com.ward;

import java.util.LinkedHashMap;
import java.util.Map;

public class WardSpecs {
    private static final Map<String, WardSpec> REGISTRY = new LinkedHashMap<>();

    public static void register(WardSpec spec) {
        if (spec == null) return;
        REGISTRY.put(spec.name(), spec);
    }

    public static WardSpec get(String name) {
        return REGISTRY.get(name);
    }
}
