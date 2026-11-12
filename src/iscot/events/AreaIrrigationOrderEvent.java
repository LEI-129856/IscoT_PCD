package iscot.events;

import iscot.enums.Areas;

/** Ordem para a rega de uma área (normalmente o Garden). */
public class AreaIrrigationOrderEvent extends HighLevelOrderEvent {
    private static final long serialVersionUID = 1L;

    private final int zone;     // 1 a 3, como no IrrigationIcon
    private final int duration; // duração da rega, não pode ser negativa

    public AreaIrrigationOrderEvent(String senderId, Areas area, boolean on, int zone, int duration) {
        super(senderId, area, on);

        if (zone < 1 || zone > 3) {
            throw new IllegalArgumentException("A zona deve ser 1, 2 ou 3.");
        }

        if (duration < 0) {
            throw new IllegalArgumentException("A duração não pode ser negativa.");
        }

        this.zone = zone;
        this.duration = duration;
    }

    public int getZone() {
        return zone;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return super.toString() + ", zona=" + zone + ", duração=" + duration;
    }
}
