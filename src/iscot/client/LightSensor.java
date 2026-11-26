package iscot.client;

import iscot.client.gui.SensorControl;
import iscot.enums.Areas;
import iscot.enums.Subscription;
import iscot.enums.Type;
import iscot.events.LightSensorEvent;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LightSensor {

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
			System.err.println("Deveria ser: LightSensor IP PORTO Área Tipo");
		}

		else if (!ClientArgs.tipoValido(args[3], Type.SENSOR)) {
			System.err.println("Este programa é um " + Type.SENSOR + ", não um " + args[3] + ".");
		}

		else {
			Areas area=Areas.valueOf(args[2]);
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
			

			JFrame janela = new JFrame("Luminosidade em "+area + " [" + ligacao.getDeviceId() + "]");
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			SensorControl sensorControl= new SensorControl( "☀", "Luminosidade - "+area, "%",
				    0, 100, 1, 50,
				    valor -> {
				    	// TODO handle change in sensor
						LightSensorEvent evento = new LightSensorEvent(ligacao.getDeviceId(), area, valor);
						System.out.println(evento);
				    });
			painel.add(sensorControl,BorderLayout.CENTER);
			janela.setContentPane(painel);
			janela.setLocation(50, 450);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
		}
	}

}
