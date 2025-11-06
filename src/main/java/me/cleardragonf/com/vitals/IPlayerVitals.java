package me.cleardragonf.com.vitals;

public interface IPlayerVitals {
    // Temperature is normalized: -100 (freezing) .. 0 (neutral) .. +100 (overheating)
    int getTemperature();
    void setTemperature(int value);
    void addTemperature(int delta);

    // Thirst is 0..100 (100 = fully hydrated)
    int getThirst();
    void setThirst(int value);
    void addThirst(int delta);

    // Convenience
    default boolean isHypothermic() { return getTemperature() <= -75; }
    default boolean isHyperthermic() { return getTemperature() >= 75; }
    default boolean isDehydrated() { return getThirst() <= 0; }
}

