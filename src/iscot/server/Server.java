package iscot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor central do IscoT (message broker).
 * Fase 4, passo 1: fica à escuta num porto e aceita ligações de clientes.
 */
public class Server {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Uso: Server PORTO");
        }

        else {
            int porto = Integer.parseInt(args[0]);
            IdGenerator ids = new IdGenerator(); // partilhado por todas as threads dos clientes

            try (ServerSocket serverSocket = new ServerSocket(porto)) {
                System.out.println("Servidor IscoT à escuta no porto " + porto);

                while (true) {
                    Socket socket = serverSocket.accept(); // bloqueia até um cliente se ligar
                    System.out.println("Nova ligação de " + socket.getRemoteSocketAddress());

                    // Cada cliente tem a sua própria thread; a principal volta logo ao accept()
                    ClientHandler handler = new ClientHandler(socket, ids);
                    handler.start(); // start(), não run(): corre numa thread nova
                }
            } catch (IOException e) {
                System.err.println("Não foi possível abrir o porto " + porto + ". Já está em uso?");
            }
        }
    }
}
