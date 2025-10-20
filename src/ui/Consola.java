package ui;

import domain.*;
import exceptions.PersistenciaException;
import repo.AulaRepo;
import repo.ReservaRepo;
import service.ReservaService;
import service.ReporteService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import static ui.InputUtils.*;

public class Consola {
    private final Scanner sc = new Scanner(System.in);
    private final AulaRepo aulaRepo;
    private final ReservaRepo reservaRepo;
    private final ReservaService service;
    private final ReporteService reporteService = new ReporteService();

    public Consola() throws PersistenciaException {
        aulaRepo = new AulaRepo("aulas.csv");
        reservaRepo = new ReservaRepo("reservas.csv");
        service = new ReservaService(aulaRepo, reservaRepo);
        service.refrescarHistoricas();
    }

    public void run(){
        while (true){
            System.out.println("\n=== Gestor de Reservas ITCA ===");
            System.out.println("1) Gestionar Aulas");
            System.out.println("2) Reservas");
            System.out.println("3) Reportes (exportación TXT)");
            System.out.println("0) Salir");
            System.out.print("> ");
            String op = sc.nextLine();
            try {
                switch (op){
                    case "1": menuAulas(); break;
                    case "2": menuReservas(); break;
                    case "3": menuReportes(); break;
                    case "0": System.out.println("Adiós"); return;
                    default: System.out.println("Opción inválida.");
                }
            } catch (Exception e){ System.out.println("Error: " + e.getMessage()); }
        }
    }

    private void menuAulas() throws Exception {
    while (true) {
        System.out.println("\n--- Aulas ---");
        System.out.println("1) Registrar nueva");
        System.out.println("2) Listar");
        System.out.println("3) Modificar");
        System.out.println("0) Volver");
        System.out.print("> ");
        String op = sc.nextLine();
        switch (op){
            case "1":
                registrarAula();
                break;
            case "2":
                var list = aulaRepo.listar();
                if (list.isEmpty()) {
                    System.out.println("No hay aulas registradas.");
                } else {
                    list.forEach(System.out::println);
                }
                break;
            case "3":
                if (aulaRepo.listar().isEmpty()) {
                    System.out.println("No hay aulas para modificar.");
                } else {
                    modificarAula();
                }
                break;
            case "0":
                return; 
            default:
                System.out.println("Opción inválida.");
        }
    }
}


    private void registrarAula() throws PersistenciaException {
    String id;
    while (true) {
        System.out.print("ID de Aula (ej.C-100): ");
        id = sc.nextLine().trim().toUpperCase();

        
        if (!id.matches("^[A-Z]-\\d{2,}$")) {
            System.out.println("Formato inválido. Use letra-guion-número (mín. 2 dígitos). Ej: C-100, L-205, A-12");
            continue;
        }

        
        if (aulaRepo.existeId(id)) {
            System.out.println("Ya existe un aula con ese ID. Intente otro.");
            continue;
        }
        break;
    }

    String nombre = ui.InputUtils.leerNoVacio(sc, "Nombre");
    TipoAula tipo = leerTipoAula();
    int cap = ui.InputUtils.leerEntero(sc, "Capacidad");
    System.out.print("Ubicación: "); String ubic = sc.nextLine();

    aulaRepo.agregar(new domain.Aula(id, nombre, tipo, cap, ubic));
    System.out.println("Aula registrada con ID " + id);
}


    private TipoAula leerTipoAula(){
        while (true){
            System.out.print("Tipo (TEORICA/LABORATORIO/AUDITORIO): ");
            try { return TipoAula.valueOf(sc.nextLine().trim().toUpperCase()); }
            catch (Exception e){ System.out.println("Tipo inválido."); }
        }
    }

    private void modificarAula() throws Exception {
        String id = leerNoVacio(sc, "ID de aula");
        Aula a = aulaRepo.porId(id);
        System.out.print("Nuevo nombre ("+a.getNombre()+"): "); String nombre = sc.nextLine();
        System.out.print("Nuevo tipo ("+a.getTipo()+"): "); String tt = sc.nextLine().trim();
        TipoAula tipo = tt.isBlank() ? a.getTipo() : TipoAula.valueOf(tt.toUpperCase());
        System.out.print("Nueva capacidad ("+a.getCapacidad()+"): "); String cc = sc.nextLine();
        int cap = cc.isBlank() ? a.getCapacidad() : Integer.parseInt(cc);
        System.out.print("Nueva ubicación ("+a.getUbicacion()+"): "); String ub = sc.nextLine();
        aulaRepo.actualizar(new Aula(id, nombre.isBlank()?a.getNombre():nombre, tipo, cap, ub.isBlank()?a.getUbicacion():ub));
        System.out.println("Aula actualizada.");
    }

    
    private void menuReservas() throws Exception {
    service.refrescarHistoricas();
    while (true) {
        System.out.println("\n--- Reservas ---");
        System.out.println("1) Registrar nueva");
        System.out.println("2) Buscar por responsable");
        System.out.println("3) Modificar");
        System.out.println("4) Cancelar");
        System.out.println("5) Listar (orden configurable)");
        System.out.println("0) Volver");
        System.out.print("> ");
        String op = sc.nextLine();
        switch (op){
            case "1":
                if (aulaRepo.listar().isEmpty()) {
                    System.out.println("Primero registre aulas (no hay aulas disponibles).");
                } else {
                    altaReserva();
                }
                break;
            case "2":
                System.out.print("Nombre de Responsable: ");
                var resultados = service.buscarPorResponsable(sc.nextLine());
                if (resultados.isEmpty()) {
                    System.out.println("Sin coincidencias.");
                } else {
                    resultados.forEach(this::imprimirReserva);
                }
                break;
            case "3":
                if (service.listarOrdenado(Comparator.comparing(Reserva::getFecha)).isEmpty()) {
                    System.out.println("No hay reservas para modificar.");
                } else {
                    modificarReservaFlow();
                }
                break;
            case "4":
                if (service.listarOrdenado(Comparator.comparing(Reserva::getFecha)).isEmpty()) {
                    System.out.println("No hay reservas para cancelar.");
                } else {
                    String idC = leerNoVacio(sc, "ID de reserva");
                    reservaRepo.actualizarEstado(idC, EstadoReserva.CANCELADA);
                    System.out.println("Reserva cancelada.");
                }
                break;
            case "5":
                var todas = service.listarOrdenado(elegirComparator());
                if (todas.isEmpty()) {
                    System.out.println("No hay reservas para listar.");
                } else {
                    todas.forEach(this::imprimirReserva);
                }
                break;
            case "0":
                return; 
            default:
                System.out.println("Opción inválida.");
        }
    }
}

    private void altaReserva() throws Exception {
    System.out.println("Tipo: 1) Clase  2) Práctica  3) Evento");
    System.out.print("> "); String t = sc.nextLine();
    if (!List.of("1","2","3").contains(t)) { System.out.println("Tipo inválido."); return; }

    String id = java.util.UUID.randomUUID().toString();
    String aulaId = leerNoVacio(sc, "Aula ID");
    LocalDate fecha = leerFecha(sc, "Fecha");      
    LocalTime ini   = leerHora(sc, "Inicio");
    LocalTime fin   = leerHora(sc, "Fin");
    String resp     = leerNoVacio(sc, "Responsable");
    System.out.print("Observaciones: "); String obs = sc.nextLine();

    Reserva r;
    if ("1".equals(t)) r = new ReservaClase(id, aulaId, fecha, ini, fin, resp, EstadoReserva.ACTIVA, obs);
    else if ("2".equals(t)) r = new ReservaPractica(id, aulaId, fecha, ini, fin, resp, EstadoReserva.ACTIVA, obs);
    else {
        TipoEvento te = leerTipoEvento();
        r = new ReservaEvento(id, aulaId, fecha, ini, fin, resp, EstadoReserva.ACTIVA, te, obs);
    }
    service.validarYRegistrar(r);
    System.out.println("Reserva creada. ID=" + id);
}


    private TipoEvento leerTipoEvento(){
        while (true){
            System.out.print("TipoEvento (CONFERENCIA/TALLER/REUNION): ");
            try { return TipoEvento.valueOf(sc.nextLine().trim().toUpperCase()); }
            catch (Exception e){ System.out.println("Valor inválido."); }
        }
    }

    private void modificarReservaFlow() throws Exception {
        String id = leerNoVacio(sc, "ID de reserva");
        System.out.println("Cambiar: 1) Fecha  2) Horario  3) Aula  4) Responsable");
        System.out.print("> "); String op = sc.nextLine();
        switch (op){
            case "1":
                LocalDate f = leerFecha(sc, "Nueva fecha");
                service.modificarReserva(id, r -> r.setFecha(f));
                break;
            case "2":
                LocalTime i = leerHora(sc, "Nuevo inicio");
                LocalTime f2 = leerHora(sc, "Nuevo fin");
                service.modificarReserva(id, r -> { r.setHoraInicio(i); r.setHoraFin(f2); });
                break;
            case "3":
                String aulaId = leerNoVacio(sc, "Nuevo Aula ID");
                service.modificarReserva(id, r -> r.setAulaId(aulaId));
                break;
            case "4":
                String resp = leerNoVacio(sc, "Nuevo responsable");
                service.modificarReserva(id, r -> r.setResponsable(resp));
                break;
            default:
                System.out.println("Opción inválida.");
                return;
        }
        System.out.println("Reserva modificada.");
    }

    private Comparator<Reserva> elegirComparator(){
        System.out.println("Ordenar por: 1) fecha 2) inicio 3) aula 4) responsable 5) duración 6) estado");
        System.out.print("> ");
        String op = sc.nextLine();
        return switch (op){
            case "1" -> Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHoraInicio);
            case "2" -> Comparator.comparing(Reserva::getHoraInicio);
            case "3" -> Comparator.comparing(Reserva::getAulaId);
            case "4" -> Comparator.comparing(Reserva::getResponsable, String.CASE_INSENSITIVE_ORDER);
            case "5" -> Comparator.comparingDouble(Reserva::duracionEnHoras).reversed();
            case "6" -> Comparator.comparing(Reserva::getEstado);
            default -> Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHoraInicio);
        };
    }

    private void imprimirReserva(Reserva r){
        System.out.printf("[%s] %s %s %s-%s Aula=%s Resp=%s Dur=%.2fh ID=%s%n",
                r.getEstado(), r.tipoReserva(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(),
                r.getAulaId(), r.getResponsable(), r.duracionEnHoras(), r.getId());
    }

    
    private void menuReportes() throws IOException, PersistenciaException {
    service.refrescarHistoricas();
    while (true) {
        System.out.println("\n--- Reportes ---");
        System.out.println("1) Top 3 aulas por horas");
        System.out.println("2) Ocupación por tipo de aula (horas)");
        System.out.println("3) Distribución por tipo de reserva (conteo)");
        System.out.println("4) Exportar a TXT");
        System.out.println("0) Volver");
        System.out.print("> ");
        String op = sc.nextLine();
        switch (op){
            case "1":
                var top3 = service.top3AulasPorHoras();
                if (top3.isEmpty()) System.out.println("No hay datos para el reporte.");
                else top3.forEach((a,h)-> System.out.println(a+" -> "+String.format(java.util.Locale.US,"%.2f h",h)));
                break;
            case "2":
                var occ = service.ocupacionPorTipoAula();
                if (occ.isEmpty()) System.out.println("No hay datos para el reporte.");
                else occ.forEach((t,h)-> System.out.println(t+" -> "+String.format(java.util.Locale.US,"%.2f h",h)));
                break;
            case "3":
                var dist = service.distribucionPorTipoReserva();
                if (dist.isEmpty()) System.out.println("No hay datos para el reporte.");
                else dist.forEach((k,v)-> System.out.println(k+" -> "+v));
                break;
            case "4":
                var t3 = service.top3AulasPorHoras();
                var oc = service.ocupacionPorTipoAula();
                var di = service.distribucionPorTipoReserva();
                if (t3.isEmpty() && oc.isEmpty() && di.isEmpty()) {
                    System.out.println("No hay datos para exportar.");
                } else {
                    new service.ReporteService().exportarTop3(t3, "top3.txt");
                    new service.ReporteService().exportarOcupacion(oc, "ocupacion.txt");
                    new service.ReporteService().exportarDistribucion(di, "distribucion.txt");
                    System.out.println("Exportados: top3.txt, ocupacion.txt, distribucion.txt");
                }
                break;
            case "0":
                return;
            default:
                System.out.println("Opción inválida.");
        }
    }
}
//
}
