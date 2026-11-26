package iscot.client;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.swing.*;

import iscot.client.gui.VentilatorIcon;
import iscot.enums.Areas;
import iscot.enums.Subscription;
import iscot.events.AreaVentilationOrderEvent;
import iscot.enums.Type;

public class Ventilator {

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
			System.err.println("Deveria ser: Ventilator IP PORTO Área Tipo");
		}

		else if (!ClientArgs.tipoValido(args[3], Type.ACTUATOR)) {
			System.err.println("Este programa é um " + Type.ACTUATOR + ", não um " + args[3] + ".");
		}

		else {
			Areas area=Areas.valueOf(args[2]);
			String tipo = args[3];

			// Ligar ao servidor e registar (fase 4)
			try {
				ligacao = ServerConnection.ligar(args[0], Integer.parseInt(args[1]), area, tipo, Subscription.VENTILATION_ORDERS);
			} catch (IOException e) {
				System.err.println("Não foi possível ligar ao servidor: " + e.getMessage());
				return;
			}
			System.out.println("Registado com o ID " + ligacao.getDeviceId());

			JFrame janela = new JFrame("Ventilador em "+ area + " [" + ligacao.getDeviceId() + "]");
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		
			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			VentilatorIcon ventilator = new VentilatorIcon();
			painel.add(ventilator,BorderLayout.CENTER);

			// Histórico de ações (secção 1.3 do enunciado)
			JTextArea historico = new JTextArea(5, 25);
			historico.setEditable(false);
			painel.add(new JScrollPane(historico), BorderLayout.SOUTH);

			janela.setContentPane(painel);
			janela.setLocation(1100, 50);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
			/*Thread.sleep(5000);
			ventilator.setAtivo(true);*/

			// TODO: receive and handle messages from server
			
			// TODO: for first phase: create an appropriate event hardcoded locally and change the GUI accordingly
			// Fase 3: ordens criadas à mão, a simular as que vão chegar do servidor
			String decisorTeste = "DecisorTeste"; // no sistema final, quem cria as ordens é um decisor
			Thread.sleep(3000);
			aplicarOrdem(ventilator, historico, new AreaVentilationOrderEvent(decisorTeste, area, true));
			Thread.sleep(3000);
			aplicarOrdem(ventilator, historico, new AreaVentilationOrderEvent(decisorTeste, area, false));
		}
	}

	/**
	 * Executa uma ordem de ventilação: muda o ícone e regista a ação no histórico.
	 * Na fase 3 é chamado pelo main com ordens de teste; na fase 5 vai ser chamado pela thread que recebe as ordens do servidor.
	 */
	private static void aplicarOrdem(VentilatorIcon ventilador, JTextArea historico, AreaVentilationOrderEvent ordem) {
		// Mudar o ícone de acordo com a ordem
		ventilador.setLigado(ordem.isOn());

		// Montar a linha do histórico: "Ventilador ligado - HH:MM:SS"
		String linha = (ordem.isOn() ? "Ventilador ligado" : "Ventilador desligado") + " - " + ordem.getFormattedTime();

		// Acrescentar ao histórico na thread do Swing (EDT)
		SwingUtilities.invokeLater(() -> historico.append(linha + "\n"));
	}
}
