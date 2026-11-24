package iscot.client;

import iscot.client.gui.IrrigationIcon;
import iscot.enums.Areas;
import iscot.enums.Type;
import iscot.events.AreaIrrigationOrderEvent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;

public class Irrigation {

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
			System.err.println("Deveria ser: Irrigation IP PORTO Área Tipo");
		}

		else if (!ClientArgs.tipoValido(args[3], Type.ACTUATOR)) {
			System.err.println("Este programa é um " + Type.ACTUATOR + ", não um " + args[3] + ".");
		}

		else {
			Areas area=Areas.valueOf(args[2]);
			String tipo = args[3];

			// Ligar ao servidor e registar (fase 4)
			try {
				ligacao = ServerConnection.ligar(args[0], Integer.parseInt(args[1]), area, tipo);
			} catch (IOException e) {
				System.err.println("Não foi possível ligar ao servidor: " + e.getMessage());
				return;
			}
			System.out.println("Registado com o ID " + ligacao.getDeviceId());

			JFrame janela = new JFrame("Rega em "+ area + " [" + ligacao.getDeviceId() + "]");
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		
			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			IrrigationIcon rega = new IrrigationIcon();
			painel.add(rega,BorderLayout.CENTER);

			// Histórico de ações (secção 1.3 do enunciado)
			JTextArea historico = new JTextArea(5, 25);
			historico.setEditable(false);
			painel.add(new JScrollPane(historico), BorderLayout.SOUTH);

			janela.setContentPane(painel);
			janela.setLocation(750, 50);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
			/*Thread.sleep(5000);
			rega.setAtiva(true);*/

			// TODO: receive and handle messages from server
			
			// TODO: for first phase: create an appropriate event hardcoded locally and change the GUI accordingly
			// Fase 3: ordens criadas à mão, a simular as que vão chegar do servidor
			String decisorTeste = "DecisorTeste"; // no sistema final, quem cria as ordens é um decisor
			Thread.sleep(3000);
			aplicarOrdem(rega, historico, new AreaIrrigationOrderEvent(decisorTeste, area, true, 1, 10));
			Thread.sleep(3000);
			aplicarOrdem(rega, historico, new AreaIrrigationOrderEvent(decisorTeste, area, true, 2, 15));
			Thread.sleep(3000);
			aplicarOrdem(rega, historico, new AreaIrrigationOrderEvent(decisorTeste, area, false, 1, 0));
		}
	}

	/**
	 * Executa uma ordem de rega: muda o ícone e regista a ação no histórico.
	 * Na fase 3 é chamado pelo main com ordens de teste; na fase 5 vai ser chamado
	 * pela thread que recebe as ordens do servidor.
	 */
	private static void aplicarOrdem(IrrigationIcon rega, JTextArea historico, AreaIrrigationOrderEvent ordem) {
		// 1. Mudar o ícone de acordo com a ordem
		rega.setAtiva(ordem.isOn());
		rega.setZona(ordem.getZone());
		rega.setDuracao(ordem.getDuration());

		// 2. Montar a linha do histórico: "Rega ligada (zona 1, 10 min) - HH:MM:SS"
		String linha;
		if (ordem.isOn()) {
			linha = "Rega ligada (zona " + ordem.getZone() + ", " + ordem.getDuration() + " min) - "
					+ ordem.getFormattedTime();
		} else {
			linha = "Rega desligada - " + ordem.getFormattedTime();
		}

		// 3. Acrescentar ao histórico na thread do Swing (EDT)
		SwingUtilities.invokeLater(() -> historico.append(linha + "\n"));
	}
}
