package me.cleardragonf.com.ward;

public record BoolOption(String key, String label, boolean defaultValue) implements OptionSpec {
    @Override public Type type() { return Type.BOOL; }
}

