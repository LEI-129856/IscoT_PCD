package iscot.events;

import iscot.enums.Areas;

/** Uma leitura de um sensor. Publicada pelos sensores, consumida pelos decisores. */
public abstract class SensorEvent extends Event {
    private static final long serialVersionUID = 1L;

    private final double value;

    protected SensorEvent(String senderId, Areas area, double value) {
        super(senderId, area);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    /** Cada tipo de sensor diz a sua unidade (%, °C, ...). */
    public abstract String getUnit();

    /** Validação comum para os sensores que medem em percentagem. */
    protected static double checkPercentage(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("O valor deve estar entre 0 e 100 %.");
        }
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + " | valor=" + value + " " + getUnit();
    }
}
