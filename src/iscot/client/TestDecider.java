package iscot.client;

import iscot.enums.AirConditioningMode;
import iscot.enums.Areas;
import iscot.enums.Subscription;
import iscot.enums.Type;
import iscot.events.AreaIrrigationOrderEvent;
import iscot.events.AreaLightingOrderEvent;
import iscot.events.AreaTemperatureOrderEvent;
import iscot.events.AreaVentilationOrderEvent;
import iscot.events.HighLevelOrderEvent;

import java.io.IOException;
import java.util.Scanner;

/**
 * Decisor de TESTE (fase 5): ainda não decide nada sozinho.
 * Mostra as leituras que recebe dos sensores e envia as ordens que se escrevem na consola,
 * para testar o reencaminhamento do servidor antes de existirem decisores a sério (fase 6).
 *
 * Uso: TestDecider IP PORTO Área DECIDER
 *
 * Comandos (um por linha):
 *   Kitchen LIGHT on 80        Kitchen LIGHT off
 *   BedRoom AC Frio 22         BedRoom AC Desligado
 *   WC VENT on                 WC VENT off
 *   Garden IRRIG on 1 10       Garden IRRIG off
 */
public class TestDecider {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Deveria ser: TestDecider IP PORTO Área Tipo");
            return;
        }
        if (!ClientArgs.tipoValido(args[3], Type.DECIDER)) {
            System.err.println("Este programa é um " + Type.DECIDER + ", não um " + args[3] + ".");
            return;
        }

        ServerConnection ligacao;
        try {
            ligacao = ServerConnection.ligar(args[0], Integer.parseInt(args[1]),
                    Areas.valueOf(args[2]), args[3], Subscription.SENSOR_READINGS);
        } catch (IOException e) {
            System.err.println("Não foi possível ligar ao servidor: " + e.getMessage());
            return;
        }
        System.out.println("Registado com o ID " + ligacao.getDeviceId());

        // As leituras dos sensores chegam pela thread de receção e são só mostradas
        ligacao.iniciarRececao(evento -> System.out.println("Leitura recebida: " + evento));

        System.out.println("Escreve uma ordem (ex.: Kitchen LIGHT on 80) ou 'sair':");
        Scanner consola = new Scanner(System.in);
        while (consola.hasNextLine()) {
            String linha = consola.nextLine().trim();
            if (linha.isEmpty()) continue;
            if (linha.equalsIgnoreCase("sair")) break;
            try {
                HighLevelOrderEvent ordem = criarOrdem(ligacao.getDeviceId(), linha.split("\\s+"));
                ligacao.enviar(ordem);
                System.out.println("Enviada: " + ordem);
            } catch (IOException e) {
                System.err.println("Ligação ao servidor perdida: " + e.getMessage());
                break;
            } catch (RuntimeException e) {
                // comando mal escrito: área/modo inexistente, número inválido, valores fora dos limites...
                System.err.println("Comando inválido (" + e.getMessage() + "). Ex.: Kitchen LIGHT on 80");
            }
        }
    }

    /** Converte uma linha da consola na ordem correspondente. */
    private static HighLevelOrderEvent criarOrdem(String id, String[] p) {
        Areas area = Areas.valueOf(p[0]);
        boolean on = p.length > 2 && p[2].equalsIgnoreCase("on");
        switch (p[1].toUpperCase()) {
            case "LIGHT":
                return new AreaLightingOrderEvent(id, area, on, on ? Integer.parseInt(p[3]) : 0);
            case "AC":
                AirConditioningMode modo = AirConditioningMode.valueOf(p[2]);
                double temp = p.length > 3 ? Double.parseDouble(p[3]) : 22;
                return new AreaTemperatureOrderEvent(id, area, modo, temp);
            case "VENT":
                return new AreaVentilationOrderEvent(id, area, on);
            case "IRRIG":
                return on ? new AreaIrrigationOrderEvent(id, area, true, Integer.parseInt(p[3]), Integer.parseInt(p[4]))
                        : new AreaIrrigationOrderEvent(id, area, false, 1, 0);
            default:
                throw new IllegalArgumentException("dispositivo desconhecido: " + p[1]);
        }
    }
}
