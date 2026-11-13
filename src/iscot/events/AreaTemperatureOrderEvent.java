package iscot.events;

import iscot.enums.AirConditioningMode;
import iscot.enums.Areas;

/** Ordem para o ar condicionado de uma divisão. */
public class AreaTemperatureOrderEvent extends HighLevelOrderEvent {
    private static final long serialVersionUID = 1L;

    private final AirConditioningMode mode;
    private final double temperature; // 16 a 30 °C, como no ACIcon

    public AreaTemperatureOrderEvent(String senderId, Areas area, AirConditioningMode mode, double temperature) {
        super(senderId, area, mode != AirConditioningMode.Desligado); // o modo já diz se é para ligar

        if (mode == null) {
            throw new IllegalArgumentException("O modo não pode ser null.");
        }

        if (temperature < 16 || temperature > 30) {
            throw new IllegalArgumentException("A temperatura deve estar entre 16 e 30 °C.");
        }
        this.mode = mode;
        this.temperature = temperature;
    }

    public AirConditioningMode getMode() {
        return mode;
    }

    public double getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return super.toString() + ", modo = " + mode + ", temperatura = " + temperature + " °C";
    }
}
