package strategy.runtime;

public class RuntimeTerminate extends RuntimeException {
    public RuntimeTerminate(String message) {
        super(message);
    }
}
