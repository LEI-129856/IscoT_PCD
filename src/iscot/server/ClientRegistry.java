package iscot.server;

import iscot.enums.Subscription;
import iscot.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista dos clientes registados no servidor (fase 5).
 * Partilhada por todas as threads ClientHandler, por isso todos os métodos são
 * synchronized: um cliente pode estar a entrar enquanto outro sai e um terceiro
 * está a reencaminhar um evento.
 */
public class ClientRegistry {

    private final List<ClientHandler> clientes = new ArrayList<>();

    /** Chamado quando um cliente é aceite no registo. */
    public synchronized void adicionar(ClientHandler cliente) {
        clientes.add(cliente);
    }

    /** Chamado quando um cliente se desliga. */
    public synchronized void remover(ClientHandler cliente) {
        clientes.remove(cliente);
    }

    /**
     * Devolve uma CÓPIA da lista, só com os clientes que devem receber este evento.
     * O envio é feito por quem chama, já fora do lock: assim um cliente lento
     * não bloqueia as outras threads que precisam da lista.
     */
    public synchronized List<ClientHandler> destinatarios(Event evento) {
        List<ClientHandler> resultado = new ArrayList<>();
        for (ClientHandler c : clientes) {
            if (interessado(c, evento)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Um cliente quer o evento se for do tipo que subscreveu e da área certa. */
    private boolean interessado(ClientHandler c, Event evento) {
        Subscription sub = c.getSubscription();
        if (!sub.aceita(evento)) {
            return false; // não é o tipo de evento que este cliente quer
        }
        if (sub == Subscription.SENSOR_READINGS) {
            return true;  // decisores (fase 6): recebem leituras de todas as áreas
        }
        return c.getArea() == evento.getArea(); // atuadores: só ordens da sua área
    }
}
