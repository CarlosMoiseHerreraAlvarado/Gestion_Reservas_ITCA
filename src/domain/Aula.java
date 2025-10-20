package domain;

import java.util.Objects;

public class Aula {
    private String id;
    private String nombre;
    private TipoAula tipo;
    private int capacidad;
    private String ubicacion;

    public Aula(String id, String nombre, TipoAula tipo, int capacidad, String ubicacion) {
        this.id = id; this.nombre = nombre; this.tipo = tipo; this.capacidad = capacidad; this.ubicacion = ubicacion;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoAula getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public String getUbicacion() { return ubicacion; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(TipoAula tipo) { this.tipo = tipo; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String toCsv() {
        return String.join(";", id, nombre, tipo.name(), String.valueOf(capacidad), ubicacion == null ? "" : ubicacion);
    }
    public static Aula fromCsv(String line) {
        String[] p = line.split(";", -1);
        return new Aula(p[0], p[1], TipoAula.valueOf(p[2]), Integer.parseInt(p[3]), p.length > 4 ? p[4] : "");
    }

    @Override public boolean equals(Object o){ return o instanceof Aula a && Objects.equals(id,a.id); }
    @Override public int hashCode(){ return Objects.hash(id); }
    @Override public String toString(){ return String.format("%s (%s) cap:%d id=%s", nombre, tipo, capacidad, id); }
}
