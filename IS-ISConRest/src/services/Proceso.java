package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Proceso extends Thread {

	public int id;
	private ArrayList<Mensaje> buzon = new ArrayList<Mensaje>();
	private ArrayList<Mensaje> buzonPersonal = new ArrayList<Mensaje>();
	private Client client = ClientBuilder.newClient();
	ArrayList<String> ips = new ArrayList<String>();
	private int reloj = 0;
	private static Semaphore semaforoBuzon = new Semaphore(1);
	private Map<Integer, String> localizadorProcesos;
	private boolean ordenacion;
	private static Semaphore semaforoFichero = new Semaphore(1);
	private String rutaLog = "/home/i0915904/Escritorio/";
	
	public Proceso(int id) {
		this.id = id;
	}
	
	public Proceso(int id, ArrayList<String> ips, Map<Integer, String> localizador, boolean ordenacion) {
		this.id = id;
		this.ips = ips;
		this.localizadorProcesos = localizador;
		this.ordenacion = ordenacion;
		try {
			semaforoFichero.acquire();
			File fichero = new File(this.rutaLog + "proceso" + this.id + ".log");
			if(fichero.exists()) {
				fichero.delete();
				fichero.createNewFile();
			}else {
					fichero.createNewFile();
			}
			semaforoFichero.release();
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}
	}
	
	//Funciones para el tratamiento del reloj logico
	public void LC1() {
		this.reloj++;
	}
	public void LC2(float orden) {
		if(orden > this.reloj) {
			this.reloj = (int) Math.floor(orden) + 1;
		}else if(orden < this.reloj) {
			this.reloj++;
		}
	}
	
	public void run() {
		for(int i = 0; i < 100; i++) {
			try {
				//Creamos el mensaje con el identificador unico
				String identificador = "P" + String.format("%02d", this.id) + " " + String.format("%03d", i);
				Mensaje mensaje = new Mensaje(0 ,identificador, 0, 0);
				//El mensaje se a�ade a un buzon personal donde estan los mensajes que se han enviado
				buzonPersonal.add(mensaje);
				//Se manda el mensaje en multicast
				for(String ip1: ips) {
					URI uri = UriBuilder.fromUri("http://" + ip1  + ":8080/IS-ISConRest").build();
					WebTarget target = client.target(uri);
					target.path("rest/Servidor/enviarMensaje").request(MediaType.APPLICATION_JSON).post(Entity.entity(mensaje, MediaType.APPLICATION_JSON));
				}
				Thread.sleep((long) (Math.random() * 500 + 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("He acabado");
	}
	
	public void recibirMensaje(Mensaje mensaje) {
		this.LC1();
		mensaje.orden = this.reloj + (float) this.id / 10;
		mensaje.estado = "PROVISIONAL";
		try {
			semaforoBuzon.acquire();
			this.buzon.add(mensaje);
			semaforoBuzon.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Se envia el mensaje en unicast
		int pid = obtenerPid(mensaje.id);
		String ipDestino = localizadorProcesos.get(pid);
		URI uri = UriBuilder.fromUri("http://" + ipDestino  + ":8080/IS-ISConRest").build();
		WebTarget target = client.target(uri);
		target.path("rest/Servidor/enviarPropuesta").request(MediaType.APPLICATION_JSON).post(Entity.entity(mensaje, MediaType.APPLICATION_JSON));
	}
	
	public void recibirPropuesta(Mensaje mensaje) {
		//Mensaje mensaje = Mensaje.fromString(msg);
		this.LC2(mensaje.orden);
		Mensaje mensajeAct = buscarMensaje(this.buzonPersonal, mensaje.id);
		mensajeAct.orden = Math.max(mensajeAct.orden, this.reloj);
		mensajeAct.orden = mensajeAct.orden + (float) this.id / 10;
		mensajeAct.SetNumPropuestas(mensajeAct.numPropuestas + 1);
		if(mensajeAct.numPropuestas == localizadorProcesos.size()) {
			mensajeAct.estado = "DEFINITIVO";
			for(String ip1: ips) {
				URI uri = UriBuilder.fromUri("http://" + ip1  + ":8080/IS-ISConRest").build();
				WebTarget target = client.target(uri);
				//String mensajeEnviar = mensajeAct.toString();
				//target.path("rest/Servidor/enviarAcuerdo").queryParam("mensaje", mensajeEnviar).request().get();
				target.path("rest/Servidor/enviarAcuerdo").request(MediaType.APPLICATION_JSON).post(Entity.entity(mensajeAct, MediaType.APPLICATION_JSON));
			}
		}
	}
	
	public void recibirAcuerdo(Mensaje mensaje) {
		//Mensaje temp = Mensaje.fromString(msg);
		this.LC2(mensaje.orden);
		actualizarBuzon(mensaje);
		try {
			semaforoBuzon.acquire();
			if(this.ordenacion) {
				this.buzon.sort(null);
			}
			if(!buzon.isEmpty()) {
				Mensaje mensajeAct = this.buzon.get(0);
				int flag = 0;
				while("DEFINITIVO".equals(mensajeAct.estado) && flag == 0) {
					FileWriter f = null;
					PrintWriter pw = null;
					try {
						f = new FileWriter(this.rutaLog + "proceso" + this.id + ".log", true);
						semaforoFichero.acquire();
						pw = new PrintWriter(f);
						pw.println("Mensaje " + mensajeAct.id + " con orden " + mensajeAct.orden + " " + mensajeAct.estado);
					} catch (IOException e) {
						e.printStackTrace();
					}finally {
						try {
							if (null != f) {
								f.close();
								pw.close();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					semaforoFichero.release();
					buzon.remove(0);
					if(buzon.isEmpty()) {
						flag = 1;
					}else {
						mensajeAct = buzon.get(0);
					}
				}
				semaforoBuzon.release();
			}else {
				semaforoBuzon.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int obtenerPid(String id) {
		String[] tokens = id.split(" ");
		String proceso = tokens[0];
		String identificador = proceso.substring(1);
		int pid = Integer.parseInt(identificador);
		return pid;
	}
	
	public Mensaje buscarMensaje(ArrayList<Mensaje> buzon, String k) {
		if(buzon == null || buzon.size() == 0) {
			System.out.println("EL BUZON ESTA VACIO");
			return null;
		}
		for(int i = 0; i<buzon.size(); i++) {
			Mensaje mensaje = buzon.get(i);
			if(mensaje == null) {
				System.out.println("EL MENSAJE ES NULL");
				return null;
			}
			if(mensaje.id.equals(k)) {
				return mensaje;
			}
		}
		System.out.println("DEVUELVO NULL "+k);
		return null;
	}
	
	public void actualizarBuzon(Mensaje temp) {
		try {
			semaforoBuzon.acquire();
			for(Mensaje msg : buzon) {
				if(msg.id.equals(temp.id)) {
					msg.orden = temp.orden;
					msg.estado = temp.estado;
					msg.numPropuestas = temp.numPropuestas;
					break;
				}
			}
			semaforoBuzon.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void enviarFicheros(String IP) {
		try {
			File fichero = new File(this.rutaLog + "comprobacion.log");
			if(fichero.exists()) {
				fichero.delete();
				fichero.createNewFile();
			}else {
					fichero.createNewFile();
			}
			URI uri = UriBuilder.fromUri("http://" + IP  + ":8080/IS-ISConRest").build();
			WebTarget target = client.target(uri);
			StringBuilder contenido = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(this.rutaLog + "proceso" + this.id + ".log"));
			String linea;
			while((linea = reader.readLine()) != null) {
				contenido.append(linea).append("/");
			}
			reader.close();
			target.path("rest/Servidor/comprobacionFicheros").request(MediaType.TEXT_PLAIN).post(Entity.entity(contenido.toString(), MediaType.TEXT_PLAIN));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void comprobarFicheros(ArrayList<String> ficheros) {
		if (ficheros == null || ficheros.isEmpty()) {
            System.out.println("No hay archivos para comparar.");
            return;
        }
		FileWriter f = null;
		PrintWriter pw = null;
		int numLineas = ficheros.get(0).split("/").length;
		try {
			f = new FileWriter(this.rutaLog + "comprobacion.log", true);
			pw = new PrintWriter(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Declaro el primer fichero como fichero referencia
		String[] referencia = ficheros.get(0).split("/");
		//Recorro todas las lineas
		for(int i = 0; i < numLineas; i++) {
			boolean todasIguales = true;
			int numFichero = 0;
			//Recorro todos los ficheros
			for(String fc: ficheros) {
				//Las lineas del fichero que toca
				String[] aComprobar = fc.split("/");
				//Comparo la linea correspondiente con el fichero referencia
				if(referencia[i].equalsIgnoreCase(aComprobar[i])) {
					//Si son iguales entonces hay que seguir con el siguiente fichero
					numFichero++;
				}else {
					//Si no son iguales lo decimos y salimos del bucle
					pw.println("Diferencia en la linea " + (i+1) + ":");
					pw.println(referencia[i] + " != " + aComprobar[i]);
					pw.println("En el fichero del proceso: " + numFichero);
					todasIguales = false;
					numFichero++;
					break;
				}
			}
			if(todasIguales) {
				pw.println("La linea " + (i+1) + " es igual en todos los ficheros: " + referencia[i]);
			}
		}
		try {
			f.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
