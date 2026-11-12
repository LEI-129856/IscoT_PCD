package iscot.events;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import iscot.enums.Areas;

/**
 * Topo da hierarquia: qualquer coisa que circula no sistema.
 * Imutável (campos final, sem setters) para poder ser lida por várias threads
 * sem sincronização, e Serializable para poder ser enviada por sockets.
 */
public abstract class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss"); //o enunciado pede neste formato

    private final String senderId;     // ID do dispositivo que criou o evento (null antes do registo)
    private final Areas area;          // divisão da casa a que o evento diz respeito
    private final LocalTime timestamp; // momento em que o evento foi criado

    protected Event(String senderId, Areas area) {
        if (area == null) {
            throw new IllegalArgumentException("A área não pode ser null.");
        }
        this.senderId = senderId;
        this.area = area;
        this.timestamp = LocalTime.now();
    }

    public String getSenderId() {
        return senderId;
    }

    public Areas getArea() {
        return area;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return timestamp.format(FORMATO);
    }

    @Override
    public String toString() {
        return "[" + getFormattedTime() + "] " + getClass().getSimpleName() + " de " + senderId + " em " + area;
    }
}
