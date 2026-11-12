package iscot.events;

import iscot.enums.Areas;

/** Leitura de humidade do solo, em percentagem (0 a 100). */
public class GroundMoistureSensorEvent extends SensorEvent {
    private static final long serialVersionUID = 1L;

    public GroundMoistureSensorEvent(String senderId, Areas area, double value) {
        super(senderId, area, checkPercentage(value));
    }

    @Override
    public String getUnit() { return "%"; }
}
