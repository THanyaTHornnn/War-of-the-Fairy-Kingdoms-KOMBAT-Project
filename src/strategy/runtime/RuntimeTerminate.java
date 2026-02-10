package strategy.runtime;

public class RuntimeTerminate extends RuntimeException {
    public RuntimeTerminate() {
        super("done");
    }
}
