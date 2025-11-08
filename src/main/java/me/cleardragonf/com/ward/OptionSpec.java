package me.cleardragonf.com.ward;

public interface OptionSpec {
    String key();
    String label();
    Type type();

    enum Type { BOOL }
}

