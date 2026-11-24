package iscot.server;

import iscot.enums.Areas;
import iscot.enums.Type;
import iscot.events.RegistrationEvent;
import iscot.events.RegistrationReply;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Thread dedicada a um cliente (secção 1.1 do enunciado).
 * Trata de toda a comunicação com esse cliente: receção e envio de eventos.
 */
public class ClientHandler extends Thread {

    private final Socket socket;
    private final String endereco; // só para as mensagens na consola
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final IdGenerator ids;

    // Preenchidos quando o registo é aceite
    private String deviceId;
    private Type tipo;
    private Areas area;

    public ClientHandler(Socket socket, IdGenerator ids) {
        this.socket = socket;
        this.ids = ids;
        this.endereco = socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void run() {
        try {
            // Ordem importante: primeiro o OutputStream (+ flush, envia o cabeçalho),
            // só depois o InputStream (que fica à espera do cabeçalho do outro lado).
            // Se os dois lados criassem primeiro o InputStream, ficavam bloqueados para sempre.
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 1. Registo: o primeiro objeto tem de ser um RegistrationEvent
            if (!registar()) {
                return; // recusado: o finally fecha a ligação
            }

            // 2. Ligação aceite: fica à espera dos eventos deste cliente
            // TODO (fase 5): tratar os eventos recebidos e encaminhá-los
            while (true) {
                Object recebido = in.readObject();
                System.out.println("Recebido de " + deviceId + ": " + recebido);
            }

        } catch (EOFException e) {
            System.out.println("Cliente " + (deviceId != null ? deviceId : endereco) + " desligou-se");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro com o cliente " + (deviceId != null ? deviceId : endereco) + ": " + e.getMessage());
        } finally {
            try {
                socket.close(); // liberta os recursos deste cliente
            } catch (IOException e) {
                // já estava fechado: não há mais nada a fazer
            }
        }
    }

    private boolean registar() throws IOException, ClassNotFoundException {
        Object primeiro = in.readObject();

        if (!(primeiro instanceof RegistrationEvent)) {
            return recusar("O primeiro objeto tem de ser um RegistrationEvent");
        }
        RegistrationEvent registo = (RegistrationEvent) primeiro;

        // Validar o tipo: tem de existir no enum Type (e o DWC é ignorado)
        Type tipoRecebido;
        try {
            tipoRecebido = Type.valueOf(registo.getClientType());
        } catch (IllegalArgumentException e) {
            return recusar("Tipo de dispositivo desconhecido: " + registo.getClientType());
        }
        if (tipoRecebido == Type.DWC) {
            return recusar("Tipo de dispositivo não suportado: DWC");
        }

        // Registo aceite: gerar o ID único e responder
        tipo = tipoRecebido;
        area = registo.getArea();
        deviceId = ids.novoId(tipo);
        out.writeObject(RegistrationReply.aceite(deviceId));
        out.flush();
        System.out.println("Registado " + deviceId + " (" + tipo + " em " + area + ") a partir de " + endereco);
        return true;
    }

    private boolean recusar(String motivo) throws IOException {
        out.writeObject(RegistrationReply.recusado(motivo));
        out.flush();
        System.out.println("Registo recusado de " + endereco + ": " + motivo);
        return false;
    }
}
