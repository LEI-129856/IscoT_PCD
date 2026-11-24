package iscot.client;

import iscot.enums.Type;

/**
 * Verificações comuns aos argumentos dos clientes (IP PORTO Área Tipo).
 */
public class ClientArgs {

    /**
     * Verifica se o tipo recebido nos argumentos é compatível com o programa.
     *
     * - Se o tipo existe no enum Type mas não é o esperado (ex.: SENSOR num atuador),
     *   o próprio cliente recusa-o e devolve false.
     * - Se o tipo não existe (ex.: XPTO), devolve true: deixa seguir, porque é o
     *   servidor que deve recusar a ligação (secção 2.1 do enunciado).
     *
     * @param tipo     o tipo recebido em args[3]
     * @param esperado o tipo que este programa é
     * @return false se o tipo existe mas é diferente do esperado; true nos outros casos
     */
    public static boolean tipoValido(String tipo, Type esperado) {
        for (Type t : Type.values()) {
            if (t.name().equals(tipo)) {
                return t == esperado;   // existe: só é válido se for o esperado
            }
        }
        return true;                    // não existe: o servidor é que recusa
    }
}
