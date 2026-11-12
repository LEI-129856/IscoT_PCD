package iscot.events;

import iscot.enums.Areas;

/** Leitura de humidade do ar, em percentagem (0 a 100). */
public class HumiditySensorEvent extends SensorEvent {
    private static final long serialVersionUID = 1L;

    public HumiditySensorEvent(String senderId, Areas area, double value) {
        super(senderId, area, checkPercentage(value));
    }

    @Override
    public String getUnit() { return "%"; }
}
