package iscot.client;

import iscot.client.gui.SensorControl;
import iscot.enums.Areas;
import iscot.events.GroundMoistureSensorEvent;

import javax.swing.*;
import java.awt.*;

public class GroundMoistureSensor {

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
		if(args.length!=3) {
			System.err.println("Invalid arguments!");
		}
		else {
			// TODO connect to the server

			// TODO register with the server
			Areas area=Areas.valueOf(args[2]);
			String senderId = "GroundMoistureSensor-" + area; // provisório: na fase 4 passa a ser o ID dado pelo servidor


			JFrame janela = new JFrame("Humidade do solo em "+area);
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			
			// Criar os componentes gráficos
			JPanel painel=new JPanel();
			painel.setLayout(new BorderLayout());
			SensorControl sensorControl= new SensorControl( "☘", "Humidade do solo - "+area, "%",
				    0, 100, 1, 50,
				    valor -> {
				    	// TODO handle change in sensor
						GroundMoistureSensorEvent evento = new GroundMoistureSensorEvent(senderId, area, valor);
						System.out.println(evento);
				    });
			painel.add(sensorControl,BorderLayout.CENTER);
			janela.setContentPane(painel);
			janela.setLocation(450, 650);
			janela.setMinimumSize(janela.getSize());
			janela.pack();
			janela.setVisible(true);
			
		}
	}

}
