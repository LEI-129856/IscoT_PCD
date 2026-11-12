package iscot.events;

import iscot.enums.Areas;

/** Leitura de luminosidade, em percentagem (0 a 100). */
public class LightSensorEvent extends SensorEvent {
    private static final long serialVersionUID = 1L;

    public LightSensorEvent(String senderId, Areas area, double value) {
        super(senderId, area, checkPercentage(value));
    }

    @Override
    public String getUnit() { return "%"; }
}
