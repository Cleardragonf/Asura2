package me.cleardragonf.com.vitals;

public class PlayerVitals implements IPlayerVitals {
    private int temperature = 0;
    private int thirst = 100;

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public void setTemperature(int value) {
        this.temperature = clamp(value, -100, 100);
    }

    @Override
    public void addTemperature(int delta) {
        setTemperature(this.temperature + delta);
    }

    @Override
    public int getThirst() {
        return thirst;
    }

    @Override
    public void setThirst(int value) {
        this.thirst = clamp(value, 0, 100);
    }

    @Override
    public void addThirst(int delta) {
        setThirst(this.thirst + delta);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}

