package exceptions;
public class PersistenciaException extends Exception {
    public PersistenciaException(String msg, Throwable cause){ super(msg, cause); }
    public PersistenciaException(String msg){ super(msg); }
}
