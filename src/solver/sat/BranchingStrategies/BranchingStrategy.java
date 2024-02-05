package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

public interface BranchingStrategy {

    Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException;
}
