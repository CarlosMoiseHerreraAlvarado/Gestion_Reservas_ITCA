package domain;

import exceptions.ValidacionException;

import java.time.*;

public class ReservaPractica extends Reserva {
    public ReservaPractica(String id, String aulaId, LocalDate fecha, LocalTime inicio, LocalTime fin, String responsable, EstadoReserva estado, String obs) {
        super(id, aulaId, fecha, inicio, fin, responsable, estado, obs);
    }

    @Override public String tipoReserva() { return "PRACTICA"; }

    @Override public void validar() throws ValidacionException {
        validarBasica();
        DayOfWeek d = fecha.getDayOfWeek();
        if (d==DayOfWeek.SATURDAY || d==DayOfWeek.SUNDAY) throw new ValidacionException("Prácticas: solo lunes a viernes.");
        if (horaInicio.isBefore(LocalTime.of(7,0)) || horaFin.isAfter(LocalTime.of(21,0))) throw new ValidacionException("Horario prácticas 07:00–21:00.");
        double h = duracionEnHoras();
        if (h < 1.0 || h > 4.0) throw new ValidacionException("Prácticas: entre 1 y 4 horas.");
    }

    public static ReservaPractica fromCsv(String[] p){
        return new ReservaPractica(p[0], p[1], LocalDate.parse(p[2]), LocalTime.parse(p[3]), LocalTime.parse(p[4]), p[5],
                EstadoReserva.valueOf(p[6]), p.length>9 ? p[9] : "");
    }
}
