package iscot.events;

import iscot.enums.Areas;

/** Leitura de temperatura ambiente, em °C. */
public class TemperatureSensorEvent extends SensorEvent {
    private static final long serialVersionUID = 1L;

    public TemperatureSensorEvent(String senderId, Areas area, double value) {
        super(senderId, area, value);
    }

    @Override
    public String getUnit() {
        return "°C";
    }
}
