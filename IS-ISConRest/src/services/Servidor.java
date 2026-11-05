package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("Servidor")
public class Servidor {
	
	private Proceso p1;
	private Proceso p2;
	private Proceso pc1;
	private Proceso pc2;
	int numAComprobar = 0;
	private Map<Integer, String> localizadorProcesos = new HashMap<Integer, String>();
	int procesosFinalizados = 0;
	int k = 0;
	ArrayList<String> ficheros = new ArrayList<String>();

	@GET
	@Path("Arrancar")
	public String arrancar(@QueryParam(value = "ordenacion") boolean ordenacion, @QueryParam(value = "ipString") String ipString, @QueryParam(value = "proceso1") int proceso1) {
		//Obtenemos las ips y las almacenamos en un Array
		localizadorProcesos.clear();
		ArrayList<String> ips = new ArrayList<>();
	    if (ipString != null && !ipString.isEmpty()) {
	        String[] ipsArray = ipString.split(",");
	        Collections.addAll(ips, ipsArray);
	    }
	    int j = 0;
	    for(int i = 0; i < ips.size(); i++) {
	    	localizadorProcesos.put(j, ips.get(i));
	    	j++;
	    	localizadorProcesos.put(j, ips.get(i));
	    	j++;
	    }

	    //Creamos los procesos de esta maquina con id, las ips, el hashmap con ips y procesos y el modo del algoritmo
	    p1 = new Proceso(proceso1, ips, localizadorProcesos, ordenacion);
	    p2 = new Proceso(proceso1 + 1, ips, localizadorProcesos, ordenacion);
	    p1.start();
	    p2.start();
	    return "Vamos a iniciar el algoritmo!";
	}
	
	@POST
	@Path("enviarMensaje")
	@Consumes(MediaType.APPLICATION_JSON)
	public void enviarMensaje(Mensaje mensaje) {
		p1.recibirMensaje(mensaje);
		p2.recibirMensaje(mensaje);
	}
	
	@POST
	@Path("enviarPropuesta")
	@Consumes(MediaType.APPLICATION_JSON)
	public void enviarPropuesta(Mensaje mensaje) {
		String[] tokens = mensaje.id.split(" ");
		String proceso = tokens[0];
		String identificador = proceso.substring(1);
		int pid = Integer.parseInt(identificador);
		if(pid == p1.id) {
			p1.recibirPropuesta(mensaje);
		}else {
			p2.recibirPropuesta(mensaje);
		}
	}
	
	@POST
	@Path("enviarAcuerdo")
	@Consumes(MediaType.APPLICATION_JSON)
	public void enviarAcuerdo(Mensaje mensaje) {
		p1.recibirAcuerdo(mensaje);
		p2.recibirAcuerdo(mensaje);
	}
	
	@GET
	@Path("pedirFicheros")
	public void pedirFicheros(@QueryParam(value = "ip") String ip, @QueryParam(value = "num") int num, @QueryParam(value = "id") int identificador) {
		procesosFinalizados = 0;
		ficheros.clear();
		k = 0;
		numAComprobar = num;
		
		pc1 = new Proceso(identificador);
	    pc2 = new Proceso(identificador+1);
		pc1.enviarFicheros(ip);
		pc2.enviarFicheros(ip);
	}
	
	//FUNCION QUE RECOGE LOS LOGS
	@POST
	@Path("comprobacionFicheros")
	@Consumes(MediaType.TEXT_PLAIN)
	public void comprobacionFicheros(String contenido) {
		procesosFinalizados++;
		ficheros.add(contenido);
		if(procesosFinalizados == numAComprobar) {
			//Mandamos a que un proceso nos compruebe los ficheros
			if(k == 0) {
				k++;
				pc1.comprobarFicheros(ficheros);
			}
		}
	}
}
