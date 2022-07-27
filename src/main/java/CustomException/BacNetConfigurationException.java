package CustomException;

public class BacNetConfigurationException extends Exception {

    public BacNetConfigurationException(String message) {
        super(message);
    }

    public BacNetConfigurationException(String message, Throwable e) {
        super(message, e);
    }
}
