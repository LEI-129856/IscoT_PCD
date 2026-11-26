package iscot.client;

import iscot.enums.Areas;
import iscot.events.Event;
import iscot.events.RegistrationEvent;
import iscot.events.RegistrationReply;
import iscot.enums.Subscription;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Ligação de um cliente ao servidor IscoT.
 * Código comum aos 8 clientes: ligar, criar os streams e fazer o registo.
 */
public class ServerConnection {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final String deviceId;

    private ServerConnection(Socket socket, ObjectOutputStream out, ObjectInputStream in, String deviceId) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.deviceId = deviceId;
    }

    /**
     * Liga-se ao servidor e regista o cliente.
     *
     * @return a ligação, já registada e com o ID atribuído pelo servidor
     * @throws IOException se não conseguir ligar ou se o registo for recusado
     */
    public static ServerConnection ligar(String ip, int porto, Areas area, String tipo, Subscription subscricao) throws IOException {
        Socket socket = new Socket(ip, porto); // lança IOException se o servidor não estiver ligado
        try {
            // Mesma ordem que no servidor: primeiro o OutputStream (+ flush), depois o InputStream
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Registo, com a subscricao
            out.writeObject(new RegistrationEvent(area, tipo, subscricao));
            out.flush();
            RegistrationReply resposta = (RegistrationReply) in.readObject();

            if (!resposta.isAccepted()) {
                socket.close();
                throw new IOException("Registo recusado: " + resposta.getMessage());
            }
            return new ServerConnection(socket, out, in, resposta.getDeviceId());

        } catch (ClassNotFoundException | ClassCastException e) {
            socket.close();
            throw new IOException("Resposta inválida do servidor", e);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    /** ID único atribuído pelo servidor. */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * NOVO (fase 5): envia um evento ao servidor.
     * synchronized para que, se duas threads deste cliente enviarem ao mesmo tempo,
     * os bytes dos dois objetos não se misturem no stream.
     */
    public synchronized void enviar(Event evento) throws IOException {
        out.writeObject(evento);
        out.flush();
        out.reset(); // esquece os objetos já enviados (senão o stream guarda referências a todos)
    }

    /**
     * Fase 5: arranca uma thread que fica sempre à espera de eventos do servidor
     * e entrega cada um ao tratador (normalmente o aplicarOrdem do atuador).
     * O readObject() bloqueia, por isso não pode correr na EDT nem no main.
     * A thread é daemon: quando a janela fecha e o main acaba, não impede o programa de terminar.
     */
    public void iniciarRececao(Consumer<Event> tratador) {
        Thread rececao = new Thread(() -> {
            try {
                while (true) {
                    Object recebido = in.readObject(); // bloqueia até chegar um evento
                    if (recebido instanceof Event) {
                        tratador.accept((Event) recebido);
                    } else {
                        System.out.println("Ignorado objeto que não é um Event: " + recebido);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Ligação ao servidor terminada: " + e.getMessage());
            }
        }, "Rececao-" + deviceId);
        rececao.setDaemon(true);
        rececao.start();
    }
}
