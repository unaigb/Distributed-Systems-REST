package client;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;


public class Cliente {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		boolean ordenacion;
		int maquinas = 0;
		String ip;
		String ipString = null;
		ArrayList<String> ips = new ArrayList<String>();
		Client client = ClientBuilder.newClient();
		boolean flag = true;

		while(flag) {
			System.out.println("==============================================================");
			System.out.println("BIENVENIDO A LA MULTIDIFUSION CON ORDENACION TOTAL POR ACUERDO");
			System.out.println("==============================================================");
			System.out.println("C) Comprobar los logs");
			System.out.println("E) Ejecutar el algoritmo");
			System.out.println("S) Salir");
			char respuesta = sc.next().charAt(0);
			respuesta = Character.toUpperCase(respuesta);
			switch(respuesta) {
				case 'C':
					System.out.println("�En cuantas maquinas vas a comprobar?");
					maquinas = sc.nextInt();
					sc.nextLine();
					System.out.println("Introduzca las IPs de las maquinas");
					for(int i = 0; i < maquinas; i++) {
						ip = sc.nextLine();
						ips.add(ip);
					}
					System.out.println("Comprobando los ficheros...");
					int proceso1 = 0;
					for(String ip1 : ips) {
						//Llamar a los servidores para que creen los procesos
						URI uri = UriBuilder.fromUri("http://" + ip1  + ":8080/IS-ISConRest").build();
						WebTarget target = client.target(uri);
						Response conexion = target.path("rest/Servidor/pedirFicheros").queryParam("ip", ips.get(0)).queryParam("num", (maquinas*2)).queryParam("id", proceso1).request().get();
						if(conexion.getStatus() < 200 || conexion.getStatus() >= 300) {
							System.out.println("La conexion a " + ip1 + " ha fallado, estatus: " + conexion.getStatus());
						}
						proceso1 += 2;
					}
					ips.clear();
					break;
				case 'E': 
					System.out.println("�Vas a querer el algoritmo con ordenacion?S/N");
					char respuesta2 = sc.next().charAt(0);
					respuesta = Character.toUpperCase(respuesta2);
					while(true) {
						if(respuesta2 == 'S') {
							//Algoritmo con ordenacion
							ordenacion = true;
							break;
						}else if(respuesta2 == 'N') {
							//Algortimo sin ordenacion
							ordenacion = false;
							break;
						}else {
							System.out.println("Introduzca solo S/N");
							respuesta2 = sc.next().charAt(0);
							respuesta2 = Character.toUpperCase(respuesta2);
						}
					}
					//Pedimos la cantidad de maquinas con las que se va a realizar el algortimo(2 procesos por maquina)
					System.out.println("Se van a utilizar 2 procesos por maquina, �Cu�ntas maquinas se van a usar?");
					maquinas = sc.nextInt();
					sc.nextLine();
					//Pedimos las ips de las maquinas
					System.out.println("Introduzca las IPs de las maquinas");
					for(int i = 0; i < maquinas; i++) {
						ip = sc.nextLine();
						ips.add(ip);
					}
					//Transformamos las ips a formato [ip1, ip2, ip3, ...] para poder pasarlas por el queryParam
					ipString = ips.stream().map(Object::toString).collect(Collectors.joining(","));
					System.out.println("Vamos a lanzar los procesos y a empezar con el algoritmo");
					proceso1 = 0;
					for(String ip1 : ips) {
						//Llamar a los servidores para que creen los procesos
						URI uri = UriBuilder.fromUri("http://" + ip1  + ":8080/IS-ISConRest").build();
						WebTarget target = client.target(uri);
						Response conexion = target.path("rest/Servidor/Arrancar").queryParam("ordenacion", ordenacion).queryParam("ipString", ipString).queryParam("proceso1", proceso1).request().get();
						if(conexion.getStatus() < 200 || conexion.getStatus() >= 300) {
							System.out.println("La conexion a " + ip1 + " ha fallado, estatus: " + conexion.getStatus());
						}
						proceso1 += 2;
					}
					ips.clear();
					break;
				case 'S':
					flag = false;
					break;
				default:
					System.out.println("OPCION NO VALIDA");
			}
		}
		sc.close();
	}
}
