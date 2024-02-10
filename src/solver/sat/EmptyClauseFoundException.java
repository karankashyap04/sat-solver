package solver.sat;

public class EmptyClauseFoundException extends Exception {

    public EmptyClauseFoundException(String errorMessage) {
        super(errorMessage);
    }
}
