package iscot.client;

import iscot.client.gui.SensorControl;
import iscot.enums.Areas;
import iscot.events.TemperatureSensorEvent;
import iscot.enums.Type;

import javax.swing.*;
import java.awt.*;

public class TemperatureSensor {

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
			System.err.println("Deveria ser: TemperatureSensor IP PORTO Área Tipo");
		}

		else if (!ClientArgs.tipoValido(args[3], Type.SENSOR)) {
			System.err.println("Este programa é um " + Type.SENSOR + ", não um " + args[3] + ".");
		}

		else {
			// TODO connect to the server

			// TODO register with the server
			Areas area=Areas.valueOf(args[2]);
			String tipo = args[3];
			String senderId = "TemperatureSensor-" + area;
			

			JFrame janela = new JFrame("Temperatura em "+area);
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			SensorControl sensorControl= new SensorControl( "♨", "Temperatura - "+area, "ºC",
				    0, 40, 0.5, 20,
				    valor -> {
						TemperatureSensorEvent evento = new TemperatureSensorEvent(senderId, area, valor);
						System.out.println(evento);
				    });
			painel.add(sensorControl,BorderLayout.CENTER);
			janela.setContentPane(painel);
			janela.setLocation(850, 400);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
		}
	}

}
