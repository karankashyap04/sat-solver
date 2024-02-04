package solver.sat;

public class NoVariableFoundException extends Exception{
    public NoVariableFoundException(String errorMessage) {
        super(errorMessage);
    }
}
