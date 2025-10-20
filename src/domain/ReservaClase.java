package domain;

import exceptions.ValidacionException;

import java.time.*;

public class ReservaClase extends Reserva {
    public ReservaClase(String id, String aulaId, LocalDate fecha, LocalTime inicio, LocalTime fin, String responsable, EstadoReserva estado, String obs) {
        super(id, aulaId, fecha, inicio, fin, responsable, estado, obs);
    }

    @Override public String tipoReserva() { return "CLASE"; }

    @Override public void validar() throws ValidacionException {
        validarBasica();
        DayOfWeek d = fecha.getDayOfWeek();
        if (d==DayOfWeek.SATURDAY || d==DayOfWeek.SUNDAY) throw new ValidacionException("Clases: solo lunes a viernes.");
        if (horaInicio.isBefore(LocalTime.of(7,0)) || horaFin.isAfter(LocalTime.of(21,0))) throw new ValidacionException("Horario clases 07:00–21:00.");
        int mins = (int) Duration.between(horaInicio, horaFin).toMinutes();
        if (mins % 45 != 0) throw new ValidacionException("Clases: duración múltiplo de 45 minutos.");
    }

    public static ReservaClase fromCsv(String[] p){
        return new ReservaClase(p[0], p[1], LocalDate.parse(p[2]), LocalTime.parse(p[3]), LocalTime.parse(p[4]), p[5],
                EstadoReserva.valueOf(p[6]), p.length>9 ? p[9] : "");
    }
}
