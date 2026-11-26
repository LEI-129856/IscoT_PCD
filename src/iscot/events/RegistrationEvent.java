package iscot.events;

import iscot.enums.Areas;
import iscot.enums.Subscription;
import iscot.enums.Type;

/** Enviado pelo cliente quando se liga ao servidor, a dizer o seu tipo e área. */
public class RegistrationEvent extends Event {
    private static final long serialVersionUID = 1L;

    private final String clientType;
    private final Subscription subscription;


    public RegistrationEvent(Areas area, String clientType, Subscription subscription) {
        super(null, area); // ainda não tem ID: é o servidor que o vai atribuir

        if (clientType == null) {
            throw new IllegalArgumentException("O tipo de cliente não pode ser null.");
        }

        if (subscription == null) {
            throw new IllegalArgumentException("A subscrição não pode ser null (usar NONE).");
        }

        this.clientType = clientType;
        this.subscription = subscription;

    }

    public String getClientType() {
        return clientType;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public String toString() {
        return super.toString() + " | tipo = " + clientType + " | subscreve = " + subscription;
    }
}
