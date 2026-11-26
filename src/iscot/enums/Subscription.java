package iscot.enums;

import iscot.events.AreaIrrigationOrderEvent;
import iscot.events.AreaLightingOrderEvent;
import iscot.events.AreaTemperatureOrderEvent;
import iscot.events.AreaVentilationOrderEvent;
import iscot.events.Event;
import iscot.events.SensorEvent;

/**
 * Que eventos um cliente quer receber do servidor.
 * Enviado no RegistrationEvent; o servidor usa o método aceita()
 * para decidir a quem entrega cada evento.
 */
public enum Subscription {
    NONE,                // sensores: só publicam, não recebem nada
    LIGHT_ORDERS,        // Light
    TEMPERATURE_ORDERS,  // AirConditioner
    VENTILATION_ORDERS,  // Ventilator
    IRRIGATION_ORDERS,   // Irrigation
    SENSOR_READINGS;     // decisores (fase 6): as 4 leituras

    /** Diz se um evento é do tipo que esta subscrição quer. */
    public boolean aceita(Event e) {
        switch (this) {
            case LIGHT_ORDERS:       return e instanceof AreaLightingOrderEvent;
            case TEMPERATURE_ORDERS: return e instanceof AreaTemperatureOrderEvent;
            case VENTILATION_ORDERS: return e instanceof AreaVentilationOrderEvent;
            case IRRIGATION_ORDERS:  return e instanceof AreaIrrigationOrderEvent;
            case SENSOR_READINGS:    return e instanceof SensorEvent;
            default:                 return false; // NONE
        }
    }

    /** Diz se é uma subscrição de ordens (as únicas válidas para atuadores). */
    public boolean isOrdem() {
        return this == LIGHT_ORDERS || this == TEMPERATURE_ORDERS
                || this == VENTILATION_ORDERS || this == IRRIGATION_ORDERS;
    }
}