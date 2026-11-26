package iscot.client;

import iscot.client.gui.ACIcon;
import iscot.enums.Areas;
import iscot.enums.AirConditioningMode;
import iscot.enums.Subscription;
import iscot.events.AreaTemperatureOrderEvent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;
import iscot.enums.Type;

public class AirConditioner {

	/**
	 * 
	 * @param server address and port
	 * @param roomName: string
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 */

	// Guardada num campo para a ligação não ser "limpa" (e fechada) quando o main termina
	private static ServerConnection ligacao;

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException, InterruptedException, ClassNotFoundException {
		if(args.length!=4) {
			System.err.println("Deveria ser: AirConditioner IP PORTO Área Tipo");
		}

		else if (!ClientArgs.tipoValido(args[3], Type.ACTUATOR)) {
			System.err.println("Este programa é um " + Type.ACTUATOR + ", não um " + args[3] + ".");
		}

		else {
			Areas area=Areas.valueOf(args[2]);
			String tipo = args[3];

			// Ligar ao servidor e registar (fase 4)
			try {
				ligacao = ServerConnection.ligar(args[0], Integer.parseInt(args[1]), area, tipo, Subscription.TEMPERATURE_ORDERS);
			} catch (IOException e) {
				System.err.println("Não foi possível ligar ao servidor: " + e.getMessage());
				return;
			}
			System.out.println("Registado com o ID " + ligacao.getDeviceId());
			
			JFrame janela = new JFrame("Ar condicionado em "+ area + " [" + ligacao.getDeviceId() + "]");
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		
			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			ACIcon ac = new ACIcon();
			painel.add(ac,BorderLayout.CENTER);

			// Histórico de ações (secção 1.3 do enunciado)
			JTextArea historico = new JTextArea(5, 25);
			historico.setEditable(false);
			painel.add(new JScrollPane(historico), BorderLayout.SOUTH);

			janela.setContentPane(painel);
			janela.setLocation(400, 50);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
			/*Thread.sleep(5000);
			ac.setModo(AirConditioningMode.Frio);
			ac.setTemperatura(22);*/

			// TODO: receive and handle messages from server
			// Fase 5: as ordens chegam do servidor. A thread de receção chama o aplicarOrdem
			// sempre que chega uma ordem deste tipo (o servidor só envia as da nossa subscrição e área).
			ligacao.iniciarRececao(evento -> {
				if (evento instanceof AreaTemperatureOrderEvent) {
					aplicarOrdem(ac, historico, (AreaTemperatureOrderEvent) evento);
				} else {
					System.out.println("Ignorado evento inesperado: " + evento);
				}
			});
			
			// TODO: for first phase: create an appropriate event hardcoded locally and change the GUI accordingly
			/*Fase 3: ordens criadas à mão, a simular as que vão chegar do servidor
			String decisorTeste = "DecisorTeste"; // no sistema final, quem cria as ordens é um decisor
			Thread.sleep(3000);
			aplicarOrdem(ac, historico, new AreaTemperatureOrderEvent(decisorTeste, area, AirConditioningMode.Frio, 22));
			Thread.sleep(3000);
			aplicarOrdem(ac, historico, new AreaTemperatureOrderEvent(decisorTeste, area, AirConditioningMode.Aquecimento, 24));
			Thread.sleep(3000);
			aplicarOrdem(ac, historico, new AreaTemperatureOrderEvent(decisorTeste, area, AirConditioningMode.Ventilação, 22));
			Thread.sleep(3000);
			aplicarOrdem(ac, historico, new AreaTemperatureOrderEvent(decisorTeste, area, AirConditioningMode.Desligado, 22));*/
		}
	}

	/**
	 * Executa uma ordem de climatização: muda o ícone e regista a ação no histórico.
	 * Na fase 3 é chamado pelo main com ordens de teste; na fase 5 vai ser chamado
	 * pela thread que recebe as ordens do servidor.
	 */
	private static void aplicarOrdem(ACIcon ac, JTextArea historico, AreaTemperatureOrderEvent ordem) {
		// 1. Mudar o ícone de acordo com a ordem (setModo também liga/desliga o ícone)
		ac.setModo(ordem.getMode());
		ac.setTemperatura(ordem.getTemperature());

		// 2. Montar a linha do histórico: "Ar condicionado em modo Frio (22.0 °C) - HH:MM:SS"
		String linha;
		if (ordem.isOn()) {
			linha = "Ar condicionado em modo " + ordem.getMode()
					+ " (" + ordem.getTemperature() + " °C) - " + ordem.getFormattedTime();
		} else {
			linha = "Ar condicionado desligado - " + ordem.getFormattedTime();
		}

		// 3. Acrescentar ao histórico na thread do Swing (EDT)
		SwingUtilities.invokeLater(() -> historico.append(linha + "\n"));
	}
}
