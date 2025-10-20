package ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputUtils {
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static LocalDate leerFecha(Scanner sc, String prompt){
        while (true){
            try {
                System.out.print(prompt + " (dd-MM-yyyy): ");
                return LocalDate.parse(sc.nextLine().trim(), DF);
            } catch (DateTimeParseException e){
                System.out.println("Fecha inválida. Ej: 19-10-2025");
            }
        }
    }

    public static LocalTime leerHora(Scanner sc, String prompt){
        while (true){
            try {
                System.out.print(prompt + " (HH:mm): ");
                return LocalTime.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e){
                System.out.println("Hora inválida. Ej: 07:30");
            }
        }
    }

    public static int leerEntero(Scanner sc, String prompt){
        while (true){
            try {
                System.out.print(prompt + ": ");
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e){
                System.out.println("Número inválido.");
            }
        }
    }

    public static String leerNoVacio(Scanner sc, String prompt){
        while (true){
            System.out.print(prompt + ": ");
            String s = sc.nextLine();
            if (s != null && !s.isBlank()) return s.trim();
            System.out.println("Valor obligatorio.");
        }
    }
}
