package server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("Servidor")
public class Servidor {
	
	private Map<Integer, String> listaMensajes = new HashMap<Integer, String>();
	int i = 0;
	private Semaphore semaforo = new Semaphore(1);
	
	@GET
	@Path("reiniciarChat")
	public void reiniciarChat() {
		i = 0;
		listaMensajes.clear();
	}
	
	@POST
	@Path("enviarChat")
	@Consumes(MediaType.TEXT_PLAIN)
	public void enviarChat(String mensaje) {
		System.out.println(mensaje);
		listaMensajes.put(i, mensaje);
		i++;
		//Cuando se envia un mensaje se añade en el hashmap
	}
	
	
	@GET
	@Path("pedirChat")
	public String pedirChat() {
		StringBuilder contenido = new StringBuilder();
		if(listaMensajes.isEmpty()) {
			return "vacio";
		}
		for(Integer key: listaMensajes.keySet()) {
			contenido.append(key).append("/").append(listaMensajes.get(key)).append("/");
		}
		return contenido.toString();
	}
	
	@POST
	@Path("modificarChat")
	@Consumes(MediaType.TEXT_PLAIN)
	public void modificarChat(String mensaje) {
		try {
			semaforo.acquire();
			String[] tokens = mensaje.split("/");
			listaMensajes.put(Integer.parseInt(tokens[0]), tokens[1]);
			semaforo.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
