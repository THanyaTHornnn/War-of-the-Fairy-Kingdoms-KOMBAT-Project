package strategy.runtime;

public class RuntimeTerminate extends RuntimeException {
    public RuntimeTerminate(String done) {
        super("done");
    }
}
