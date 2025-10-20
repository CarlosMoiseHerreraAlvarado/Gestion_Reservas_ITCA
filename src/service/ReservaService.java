package service;

import domain.*;
import exceptions.*;
import repo.AulaRepo;
import repo.ReservaRepo;

import java.util.*;
import java.util.stream.Collectors;

public class ReservaService {
    private final AulaRepo aulaRepo;
    private final ReservaRepo reservaRepo;

    public ReservaService(AulaRepo aulaRepo, ReservaRepo reservaRepo){
        this.aulaRepo = aulaRepo; this.reservaRepo = reservaRepo;
    }

    public void validarYRegistrar(Reserva r) throws Exception {
        r.validar();
        TipoAula tipoAula = aulaRepo.porId(r.getAulaId()).getTipo();
        if (r instanceof ReservaPractica && tipoAula != TipoAula.LABORATORIO)
            throw new ValidacionException("La práctica requiere LABORATORIO.");
        if ((r instanceof ReservaClase || r instanceof ReservaPractica) && tipoAula == TipoAula.AUDITORIO)
            throw new ValidacionException("Clases/Prácticas no se reservan en AUDITORIO.");
        boolean solapa = reservaRepo.porAulaYFecha(r.getAulaId(), r.getFecha()).stream()
                .filter(x -> x.getEstado() == EstadoReserva.ACTIVA)
                .anyMatch(x -> r.traslapaCon(x));
        if (solapa) throw new SolapamientoException("Conflicto: otra reserva activa solapa.");
        reservaRepo.agregar(r);
    }

    public void modificarReserva(String id, java.util.function.Consumer<Reserva> mutador) throws Exception {
        Reserva original = reservaRepo.porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Reserva no existe"));
        if (original.getEstado() == EstadoReserva.CANCELADA) throw new ValidacionException("No se puede modificar una reserva cancelada.");

        Reserva copia = clonar(original);
        mutador.accept(copia);
        copia.validar();

        TipoAula tipoAula = aulaRepo.porId(copia.getAulaId()).getTipo();
        if (copia instanceof ReservaPractica && tipoAula != TipoAula.LABORATORIO)
            throw new ValidacionException("La práctica requiere LABORATORIO.");
        if ((copia instanceof ReservaClase || copia instanceof ReservaPractica) && tipoAula == TipoAula.AUDITORIO)
            throw new ValidacionException("Clases/Prácticas no se reservan en AUDITORIO.");

        boolean solapa = reservaRepo.porAulaYFecha(copia.getAulaId(), copia.getFecha()).stream()
                .filter(x -> x.getEstado() == EstadoReserva.ACTIVA && !x.getId().equals(original.getId()))
                .anyMatch(x -> copia.traslapaCon(x));
        if (solapa) throw new SolapamientoException("Conflicto: solapa con otra reserva activa.");

        mutador.accept(original);
        reservaRepo.actualizar();
    }

    private Reserva clonar(Reserva r){
        if (r instanceof ReservaClase)
            return new ReservaClase(r.getId(), r.getAulaId(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getResponsable(), r.getEstado(), "");
        if (r instanceof ReservaPractica)
            return new ReservaPractica(r.getId(), r.getAulaId(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getResponsable(), r.getEstado(), "");
        if (r instanceof ReservaEvento)
            return new ReservaEvento(r.getId(), r.getAulaId(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getResponsable(), r.getEstado(), ((ReservaEvento) r).getTipoEvento(), "");
        throw new IllegalArgumentException("Tipo no soportado");
    }

    public Map<String, Double> top3AulasPorHoras(){
        Map<String, Double> horas = new HashMap<>();
        for (Reserva r : reservaRepo.listar()) if (r.getEstado() != EstadoReserva.CANCELADA)
            horas.merge(r.getAulaId(), r.duracionEnHoras(), Double::sum);
        return horas.entrySet().stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));
    }

    public Map<TipoAula, Double> ocupacionPorTipoAula(){
        Map<TipoAula, Double> acum = new EnumMap<>(TipoAula.class);
        
        for (Reserva r : reservaRepo.listar()) {
            try {
                TipoAula t = aulaRepo.porId(r.getAulaId()).getTipo();
                acum.merge(t, r.duracionEnHoras(), Double::sum);
            } catch (Exception ignored) {}
        }
        return acum;
    }

    public Map<String, Long> distribucionPorTipoReserva(){
        return reservaRepo.listar().stream()
                .collect(Collectors.groupingBy(Reserva::tipoReserva, Collectors.counting()));
    }

    public List<Reserva> buscarPorResponsable(String q){ return reservaRepo.buscarPorResponsable(q); }
   //Lista de Servicio
    public List<Reserva> listarOrdenado(Comparator<Reserva> cmp){
        return reservaRepo.listar().stream().sorted(cmp).toList();
    }

    public void refrescarHistoricas() throws PersistenciaException { reservaRepo.refrescarHistoricas(); }
}
