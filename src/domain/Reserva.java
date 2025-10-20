package domain;

import exceptions.ValidacionException;

import java.time.*;

public abstract class Reserva implements Validable {
    protected String id, aulaId, responsable, observaciones;
    protected LocalDate fecha;
    protected LocalTime horaInicio, horaFin;
    protected EstadoReserva estado;
    protected LocalDateTime creadaEl = LocalDateTime.now();

    protected Reserva(String id, String aulaId, LocalDate fecha, LocalTime ini, LocalTime fin, String responsable, EstadoReserva estado, String obs) {
        this.id=id; this.aulaId=aulaId; this.fecha=fecha; this.horaInicio=ini; this.horaFin=fin; this.responsable=responsable; this.estado=estado; this.observaciones=obs;
    }

    public String getId(){ return id; }
    public String getAulaId(){ return aulaId; }
    public LocalDate getFecha(){ return fecha; }
    public LocalTime getHoraInicio(){ return horaInicio; }
    public LocalTime getHoraFin(){ return horaFin; }
    public String getResponsable(){ return responsable; }
    public EstadoReserva getEstado(){ return estado; }

    public void setEstado(EstadoReserva e){ estado=e; }
    public void setFecha(LocalDate f){ fecha=f; }
    public void setHoraInicio(LocalTime t){ horaInicio=t; }
    public void setHoraFin(LocalTime t){ horaFin=t; }
    public void setAulaId(String id){ aulaId=id; }
    public void setResponsable(String r){ responsable=r; }

    public boolean traslapaCon(Reserva otra){
        if(!aulaId.equals(otra.aulaId)) return false;
        if(!fecha.equals(otra.fecha)) return false;
        return horaInicio.isBefore(otra.horaFin) && horaFin.isAfter(otra.horaInicio);
    }

    public double duracionEnHoras(){ return Duration.between(horaInicio, horaFin).toMinutes() / 60.0; }

    protected void validarBasica() throws ValidacionException {
        if (fecha==null || horaInicio==null || horaFin==null) throw new ValidacionException("Fecha y horas obligatorias.");
        if (!horaInicio.isBefore(horaFin)) throw new ValidacionException("Inicio debe ser antes que fin.");
        if (responsable==null || responsable.isBlank()) throw new ValidacionException("Responsable obligatorio.");
        if (duracionEnHoras()<=0) throw new ValidacionException("Duración inválida.");
    }

    public abstract String tipoReserva();

    public String baseCsv(){
        return String.join(";", id, aulaId, fecha.toString(), horaInicio.toString(), horaFin.toString(),
                responsable.replace(";"," "), estado.name(), tipoReserva(), observaciones==null?"":observaciones.replace(";"," "));
    }
}
