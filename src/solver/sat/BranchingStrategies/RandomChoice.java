package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

public class RandomChoice implements BranchingStrategy{

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        for (Integer var : instance.vars) {
            return var;
        }
        throw new NoVariableFoundException("No variable found in SATInstance: " + instance);
    }
}
