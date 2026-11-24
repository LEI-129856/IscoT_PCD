package iscot.server;

import iscot.enums.Type;

/**
 * Gera identificadores únicos em todo o sistema (secção 2.1 do enunciado).
 *
 * Um só objeto é partilhado por todas as threads ClientHandler. O método é
 * synchronized para que dois clientes a registar-se ao mesmo tempo nunca
 * recebam o mesmo ID: "proximo++" não é atómico (lê, soma, escreve), e sem
 * exclusão mútua duas threads podiam ler o mesmo valor.
 */
public class IdGenerator {

    private int proximo = 1;

    /** Devolve um ID novo, por exemplo "SENSOR-3". */
    public synchronized String novoId(Type tipo) {
        String id = tipo + "-" + proximo;
        proximo++;
        return id;
    }
}