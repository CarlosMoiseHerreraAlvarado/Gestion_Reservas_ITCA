package repo;

import domain.Aula;
import domain.TipoAula;
import exceptions.PersistenciaException;
import exceptions.RecursoNoEncontradoException;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class AulaRepo {
    private final List<Aula> aulas = new ArrayList<>();
    private final Path archivo;

    public AulaRepo(String rutaArchivo) throws PersistenciaException {
        this.archivo = Paths.get(rutaArchivo);
        cargar();
    }

    public List<Aula> listar(){ return Collections.unmodifiableList(aulas); }

    public void agregar(Aula a) throws PersistenciaException {
        aulas.add(a); guardar();
    }

    public Aula porId(String id) throws RecursoNoEncontradoException {
        return aulas.stream().filter(a -> a.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Aula no encontrada: " + id));
    }

    public void actualizar(Aula aula) throws PersistenciaException, RecursoNoEncontradoException {
        Aula e = porId(aula.getId());
        e.setNombre(aula.getNombre());
        e.setTipo(aula.getTipo());
        e.setCapacidad(aula.getCapacidad());
        e.setUbicacion(aula.getUbicacion());
        guardar();
    }

    public List<Aula> porTipo(TipoAula t){ return aulas.stream().filter(a->a.getTipo()==t).collect(Collectors.toList()); }

    private void cargar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo)) return;
            for (String line : Files.readAllLines(archivo)) if (!line.isBlank()) aulas.add(Aula.fromCsv(line));
        } catch (IOException e) { throw new PersistenciaException("Error cargando aulas", e); }
    }

    private void guardar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo)) Files.createFile(archivo);
            try (BufferedWriter bw = Files.newBufferedWriter(archivo)) {
                for (Aula a : aulas) { bw.write(a.toCsv()); bw.newLine(); }
            }
        } catch (IOException e){ throw new PersistenciaException("Error guardando aulas", e); }
    }
    public boolean existeId(String id) {
    String x = id.trim();
    return listar().stream().anyMatch(a -> a.getId().equalsIgnoreCase(x));
}

}

