import ui.Consola;

public class Main {
    public static void main(String[] args) {
        try { new Consola().run(); }
        catch (Exception e) { System.out.println("Error fatal: " + e.getMessage()); }
    }
}
