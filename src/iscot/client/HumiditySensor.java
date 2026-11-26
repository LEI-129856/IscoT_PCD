package iscot.client;

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import iscot.client.gui.SensorControl;
import iscot.enums.Areas;
import iscot.enums.Subscription;
import iscot.enums.Type;
import iscot.events.HumiditySensorEvent;

public class HumiditySensor {

	/**
	 * 
	 * @param server address and port
	 * @param roomName: string
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws NumberFormatException, InterruptedException {
		if(args.length!=4) {
			System.err.println("Deveria ser: HumiditySensor IP PORTO Área Tipo");
		}

		else if (!ClientArgs.tipoValido(args[3], Type.SENSOR)) {
			System.err.println("Este programa é um " + Type.SENSOR + ", não um " + args[3] + ".");
		}

		else {
			Areas area = Areas.valueOf(args[2]);
			String tipo = args[3];

			// Ligar ao servidor e registar (fase 4)
			ServerConnection ligacao;
			try {
				ligacao = ServerConnection.ligar(args[0], Integer.parseInt(args[1]), area, tipo, Subscription.NONE);
			} catch (IOException e) {
				System.err.println("Não foi possível ligar ao servidor: " + e.getMessage());
				return;
			}

			System.out.println("Registado com o ID " + ligacao.getDeviceId());

			JFrame janela = new JFrame("Humidade em "+ area + " [" + ligacao.getDeviceId() + "]");
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			SensorControl sensorControl= new SensorControl( "☂", "Humidade - "+area, "%",
				    0, 100, 1, 50,
				    valor -> {
				    	// TODO handle change in sensor
						HumiditySensorEvent evento = new HumiditySensorEvent(ligacao.getDeviceId(), area, valor);
						System.out.println(evento);
				    });
			painel.add(sensorControl,BorderLayout.CENTER);
			janela.setContentPane(painel);
			janela.setLocation(850, 450);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
		}
	}

}
