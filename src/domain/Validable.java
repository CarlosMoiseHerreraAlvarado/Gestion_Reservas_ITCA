package domain;
import exceptions.ValidacionException;
public interface Validable {
    void validar() throws ValidacionException;
}
