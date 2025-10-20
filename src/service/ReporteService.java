package service;

import domain.TipoAula;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class ReporteService {
    public void exportarTop3(Map<String, Double> top3, String ruta) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write("Top 3 Aulas por horas reservadas\n");
            for (Map.Entry<String, Double> e : top3.entrySet())
                bw.write(String.format(Locale.US, "%s -> %.2f h\n", e.getKey(), e.getValue()));
        }
    }
    public void exportarOcupacion(Map<TipoAula, Double> ocup, String ruta) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write("Ocupación por Tipo de Aula (horas acumuladas)\n");
            for (Map.Entry<TipoAula, Double> e : ocup.entrySet())
                bw.write(String.format(Locale.US, "%s -> %.2f h\n", e.getKey(), e.getValue()));
        }
    }
    public void exportarDistribucion(Map<String, Long> dist, String ruta) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write("Distribución por Tipo de Reserva (conteo)\n");
            for (Map.Entry<String, Long> e : dist.entrySet())
                bw.write(String.format("%s -> %d\n", e.getKey(), e.getValue()));
        }
    }
}
