package domain;

import exceptions.ValidacionException;

import java.time.*;

public class ReservaEvento extends Reserva {
    private TipoEvento tipoEvento;

    public ReservaEvento(String id, String aulaId, LocalDate fecha, LocalTime inicio, LocalTime fin, String responsable, EstadoReserva estado, TipoEvento tipoEvento, String obs) {
        super(id, aulaId, fecha, inicio, fin, responsable, estado, obs);
        this.tipoEvento = tipoEvento;
    }

    public TipoEvento getTipoEvento(){ return tipoEvento; }

    @Override public String tipoReserva() { return "EVENTO"; }

    @Override public void validar() throws ValidacionException {
        validarBasica();
        if (!fecha.isAfter(LocalDate.now().plusDays(1))) throw new ValidacionException("Eventos: reservar con al menos 2 días de anticipación.");
        if (duracionEnHoras() > 8.0) throw new ValidacionException("Eventos: máximo 8 horas.");
        if (horaInicio.isBefore(LocalTime.of(7,0)) || horaFin.isAfter(LocalTime.of(21,0))) throw new ValidacionException("Horario eventos 07:00–21:00.");
    }

    @Override public String baseCsv(){ return super.baseCsv() + ";" + tipoEvento.name(); }

    public static ReservaEvento fromCsv(String[] p){
        return new ReservaEvento(p[0], p[1], LocalDate.parse(p[2]), LocalTime.parse(p[3]), LocalTime.parse(p[4]), p[5],
                EstadoReserva.valueOf(p[6]), TipoEvento.valueOf(p[9]), p.length>10 ? p[10] : "");
    }
}
