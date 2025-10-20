package repo;

import domain.*;
import exceptions.PersistenciaException;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReservaRepo {
    private final List<Reserva> reservas = new ArrayList<>();
    private final Path archivo;

    public ReservaRepo(String ruta) throws PersistenciaException {
        this.archivo = Paths.get(ruta);
        cargar();
    }

    public List<Reserva> listar(){ return Collections.unmodifiableList(reservas); }

    public void agregar(Reserva r) throws PersistenciaException { reservas.add(r); guardar(); }

    public Optional<Reserva> porId(String id){ return reservas.stream().filter(r->r.getId().equals(id)).findFirst(); }

    public void actualizar() throws PersistenciaException { guardar(); }

    public void actualizarEstado(String id, EstadoReserva nuevo) throws PersistenciaException { porId(id).ifPresent(r -> r.setEstado(nuevo)); guardar(); }

    public List<Reserva> porAulaYFecha(String aulaId, LocalDate fecha){
        return reservas.stream().filter(r -> r.getAulaId().equals(aulaId) && r.getFecha().equals(fecha)).collect(Collectors.toList());
    }

    public List<Reserva> buscarPorResponsable(String texto){
        String q = texto.toLowerCase();
        return reservas.stream().filter(r -> r.getResponsable().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public void refrescarHistoricas() throws PersistenciaException {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        boolean cambio=false;
        for (Reserva r : reservas) {
            if (r.getEstado()==EstadoReserva.CANCELADA) continue;
            boolean pasada = r.getFecha().isBefore(hoy) || (r.getFecha().isEqual(hoy) && r.getHoraFin().isBefore(ahora));
            if (pasada && r.getEstado()!=EstadoReserva.HISTORICA) { r.setEstado(EstadoReserva.HISTORICA); cambio=true; }
        }
        if (cambio) guardar();
    }

    private void cargar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo)) return;
            for (String line : Files.readAllLines(archivo)) {
                if (line.isBlank()) continue;
                String[] p = line.split(";", -1);
                String tipo = p[7];
                switch (tipo){
                    case "CLASE": reservas.add(ReservaClase.fromCsv(p)); break;
                    case "PRACTICA": reservas.add(ReservaPractica.fromCsv(p)); break;
                    case "EVENTO": reservas.add(ReservaEvento.fromCsv(p)); break;
                }
            }
        } catch (IOException e){ throw new PersistenciaException("Error cargando reservas", e); }
    }

    //metod aguarda

    private void guardar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo)) Files.createFile(archivo);
            try (BufferedWriter bw = Files.newBufferedWriter(archivo)) {
                for (Reserva r : reservas) { bw.write(r.baseCsv()); bw.newLine(); }
            }
        } catch (IOException e){ throw new PersistenciaException("Error guardando reservas", e); }
    }
}
