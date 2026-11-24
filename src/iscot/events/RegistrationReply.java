package iscot.events;

import java.io.Serializable;

/**
 * Resposta do servidor a um RegistrationEvent.
 * Não é um Event (não tem área nem vai para nenhum tópico): é só a resposta
 * ao pedido de registo. Imutável e Serializable, tal como os eventos.
 */
public class RegistrationReply implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean accepted;
    private final String deviceId; // null se a ligação foi recusada
    private final String message;  // "Registo aceite" ou o motivo da recusa

    private RegistrationReply(boolean accepted, String deviceId, String message) {
        this.accepted = accepted;
        this.deviceId = deviceId;
        this.message = message;
    }

    /** Resposta de registo aceite, com o ID atribuído. */
    public static RegistrationReply aceite(String deviceId) {
        return new RegistrationReply(true, deviceId, "Registo aceite");
    }

    /** Resposta de registo recusado, com o motivo. */
    public static RegistrationReply recusado(String motivo) {
        return new RegistrationReply(false, null, motivo);
    }

    public boolean isAccepted() { return accepted; }
    public String getDeviceId() { return deviceId; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return accepted ? "Aceite com o ID " + deviceId : "Recusado: " + message;
    }
}
