package client;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class Cliente {
	
	public static void main(String[] args) {
		
		String ip = "0";
		Scanner sc = new Scanner(System.in);
		Client client = ClientBuilder.newClient();
		boolean flag = true;
		
		System.out.println("==================");
		System.out.println("BIENVENIDO AL CHAT");
		System.out.println("==================");
		System.out.println("¿Cual es la ip del servidor del grupo?");
		ip = sc.nextLine();
		//Hacer que el server reinicie el chat
		URI uri = UriBuilder.fromUri("http://" + ip + ":8080/ChatConRest").build();
		WebTarget target = client.target(uri);
		target.path("rest/Servidor/reiniciarChat").request().get();
		System.out.println("¿Cual es tu nick?");
		String nick = sc.nextLine();
		
		MarcoCliente mimarco = new MarcoCliente(ip);
		mimarco.setSize(250,350);
		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while(flag) {
			System.out.println("E) Enviar mensaje");
			System.out.println("M) Modificar mensaje");
			System.out.println("S) Salir");
			char respuesta = sc.next().charAt(0);
			respuesta = Character.toUpperCase(respuesta);
			sc.nextLine();
			switch(respuesta) {
				case 'E':
					System.out.println("¿Que mensaje quieres enviar?");
					String msg = sc.nextLine();
					String mensaje = nick + ": " + msg;
					uri = UriBuilder.fromUri("http://" + ip + ":8080/ChatConRest").build();
					target = client.target(uri);
					target.path("rest/Servidor/enviarChat").request(MediaType.TEXT_PLAIN).post(Entity.entity(mensaje, MediaType.TEXT_PLAIN));
					break;
				case 'M':
					System.out.println("¿Que mensaje quieres cambiar?");
					for(Integer key: LaminaMarcoCliente.listaMensajes.keySet()) {
						System.out.println(key + ") " + LaminaMarcoCliente.listaMensajes.get(key));
					}
					int opcion = sc.nextInt();
					sc.nextLine();
					System.out.println("¿Que mensaje deseas poner?");
					String cambio = sc.nextLine();
					String[] tokens = LaminaMarcoCliente.listaMensajes.get(opcion).split(":");
					String nuevoMensaje = opcion + "/" + tokens[0] + ": " + cambio + " (Editado por: " + nick +  ")";
					uri = UriBuilder.fromUri("http://" + ip + ":8080/ChatConRest").build();
					target = client.target(uri);
					target.path("rest/Servidor/modificarChat").request(MediaType.TEXT_PLAIN).post(Entity.entity(nuevoMensaje, MediaType.TEXT_PLAIN));
					break;
				case 'S':
					flag = false;
					break;
			}
		}
		sc.close();
	}
}

class MarcoCliente extends JFrame{
	
	public MarcoCliente(String ip) {
		
		setBounds(600,300,200,350);
		
		LaminaMarcoCliente milamina = new LaminaMarcoCliente(ip);
		
		add(milamina);
		
		setVisible(true);
	}
}

class LaminaMarcoCliente extends JPanel implements Runnable{
	
	public JTextArea campoChat;
	public JScrollPane scroll;
	private Client client = ClientBuilder.newClient();
	public static Map<Integer, String> listaMensajes = new HashMap<Integer, String>();
	public String ipServer;
	
	public LaminaMarcoCliente(String ip) {
		ipServer = ip;

		JLabel texto = new JLabel("CHAT");
		
		add(texto);
		
		campoChat = new JTextArea(16,20);
		
		scroll = new JScrollPane(campoChat);
		
		add(scroll);
		
		Thread mihilo = new Thread(this);
		
		mihilo.start();
		
	}
	
	public void run() {
		//Peticiones get cada segundo para actualizar el hashmap y actualizar la lista
		while(true) {
			//System.out.println(Cliente.ip);
			if(!"0".equals(ipServer)) {
				URI uri = UriBuilder.fromUri("http://" + ipServer + ":8080/ChatConRest").build();
				WebTarget target = client.target(uri);
				Response response = target.path("rest/Servidor/pedirChat").request(MediaType.TEXT_PLAIN).get();
				String contenido = response.readEntity(String.class);
				if("vacio".equals(contenido)) {
					
				}else {
					listaMensajes.clear();
					String[] lineas = contenido.split("/");
					for(int i = 0; i < lineas.length; i+=2) {
						listaMensajes.put(Integer.parseInt(lineas[i]), lineas[i+1]);
					}
					campoChat.setText("");
					for(Integer key: listaMensajes.keySet()) {
						campoChat.append(listaMensajes.get(key) + "\n");
					}
				}
				try {
					Thread.sleep((long) (Math.random() * 500 + 500));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
