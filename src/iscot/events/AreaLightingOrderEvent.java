package iscot.events;

import iscot.enums.Areas;

/** Ordem para as luzes de uma divisão. */
public class AreaLightingOrderEvent extends HighLevelOrderEvent {
    private static final long serialVersionUID = 1L;

    private final int intensity; // 0 a 100, como no LightIcon

    public AreaLightingOrderEvent(String senderId, Areas area, boolean on, int intensity) {
        super(senderId, area, on);
        if (intensity < 0 || intensity > 100) {
            throw new IllegalArgumentException("A intensidade deve estar entre 0 e 100.");
        }
        this.intensity = intensity;
    }

    public int getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return super.toString() + ", intensidade=" + intensity;
    }
}
