package services;

public class Mensaje implements Comparable<Mensaje> {

	public int texto;
	public String  id;
	public float orden;
	public int numPropuestas;
	public String estado;
	
	public Mensaje() {
		
	}
	
	public Mensaje(int m, String k, float orden, int numPropuestas) {
		this.texto = m;
		this.id = k;
		this.orden = orden;
		this.numPropuestas = numPropuestas;
	}
	
	public void SetNumPropuestas(int numPropuestas) {
		this.numPropuestas = numPropuestas;
	}
	
	public float getOrden() {
		return this.orden;
	}
	
	@Override
	public int compareTo(Mensaje otroMensaje) {
		return Float.compare(this.orden, otroMensaje.orden);
	}
	
}
