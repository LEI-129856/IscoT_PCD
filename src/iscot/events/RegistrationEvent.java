package iscot.events;

import iscot.enums.Areas;
import iscot.enums.Type;

/** Enviado pelo cliente quando se liga ao servidor, a dizer o seu tipo e área. */
public class RegistrationEvent extends Event {
    private static final long serialVersionUID = 1L;

    private final Type clientType;

    public RegistrationEvent(Areas area, Type clientType) {
        super(null, area); // ainda não tem ID: é o servidor que o vai atribuir
        if (clientType == null) {
            throw new IllegalArgumentException("O tipo de cliente não pode ser null.");
        }
        this.clientType = clientType;
    }

    public Type getClientType() {
        return clientType;
    }

    @Override
    public String toString() {
        return super.toString() + " | tipo = " + clientType;
    }
}
