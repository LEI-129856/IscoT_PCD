package iscot.server;

import iscot.enums.Areas;
import iscot.enums.Subscription;
import iscot.enums.Type;
import iscot.events.Event;
import iscot.events.RegistrationEvent;
import iscot.events.RegistrationReply;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
    private final ClientRegistry clientes;

    // Preenchidos quando o registo é aceite
    private String deviceId;
    private Type tipo;
    private Areas area;
    private Subscription subscription;

    public ClientHandler(Socket socket, IdGenerator ids, ClientRegistry clientes) {
        this.socket = socket;
        this.ids = ids;
        this.clientes = clientes;
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
            while (true) {
                Object recebido = in.readObject();
                if (recebido instanceof Event) {
                    reencaminhar((Event) recebido);
                } else {
                    System.out.println("Ignorado objeto que não é um Event, de " + deviceId + ": " + recebido);
                }
            }

        } catch (EOFException e) {
            System.out.println("Cliente " + (deviceId != null ? deviceId : endereco) + " desligou-se");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro com o cliente " + (deviceId != null ? deviceId : endereco) + ": " + e.getMessage());
        } finally {
            clientes.remover(this);
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

        //Validar se a subscrição faz sentido para este tipo
        Subscription sub = registo.getSubscription();
        if (!subscricaoValida(tipoRecebido, sub)) {
            return recusar("Subscrição " + sub + " inválida para um " + tipoRecebido);
        }

        // Registo aceite: gerar o ID único e responder
        tipo = tipoRecebido;
        area = registo.getArea();
        subscription = sub;
        deviceId = ids.novoId(tipo);
        out.writeObject(RegistrationReply.aceite(deviceId));
        out.flush();
        clientes.adicionar(this); // NOVO: a partir daqui pode receber eventos
        System.out.println("Registado " + deviceId + " (" + tipo + " em " + area
                + ", subscreve " + subscription + ") a partir de " + endereco);
        return true;
    }

    /**
     * Fase5: entrega um evento recebido deste cliente a todos os interessados.
     * A lista de destinatários é uma cópia, por isso os envios são feitos fora do
     * lock do ClientRegistry.
     */
    private void reencaminhar(Event evento) {
        List<ClientHandler> destinos = clientes.destinatarios(evento);
        System.out.println("Recebido de " + deviceId + ": " + evento + " -> " + destinos.size() + " destinatário(s)");
        for (ClientHandler destino : destinos) {
            try {
                destino.enviar(evento);
            } catch (IOException e) {
                // A ligação desse cliente falhou: a thread dele vai detetá-lo e removê-lo da lista
                System.out.println("Não foi possível enviar a " + destino.getDeviceId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Fase5: envia um evento a ESTE cliente.
     * synchronized porque várias threads (as dos outros clientes) podem chamar este
     * método ao mesmo tempo, e dois writeObject em simultâneo no mesmo stream
     * misturavam os bytes e corrompiam a ligação.
     */
    public synchronized void enviar(Event evento) throws IOException {
        out.writeObject(evento);
        out.flush();
        out.reset(); // esquece os objetos já enviados (senão o stream guarda referências a todos)
    }

    /** Cada tipo de cliente só pode ter certas subscrições. */
    private boolean subscricaoValida(Type tipo, Subscription sub) {
        switch (tipo) {
            case SENSOR:   return sub == Subscription.NONE;            // sensores só publicam
            case ACTUATOR: return sub.isOrdem();                       // atuadores recebem ordens
            case DECIDER:  return sub == Subscription.SENSOR_READINGS; // decisores recebem leituras
            default:       return false;
        }
    }

    private boolean recusar(String motivo) throws IOException {
        out.writeObject(RegistrationReply.recusado(motivo));
        out.flush();
        System.out.println("Registo recusado de " + endereco + ": " + motivo);
        return false;
    }

    // NOVO: getters usados pelo servidor no passo 7, para decidir a quem entregar cada evento
    public String getDeviceId() {
        return deviceId;
    }

    public Areas getArea() {
        return area;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}
