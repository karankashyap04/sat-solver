package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.Map;
import java.util.Set;

public class FirstVar implements BranchingStrategy{

    public void setContext(Set<Integer> remainingClauses, Map<Integer, Set<Integer>> globalRemovedLiterals) {
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        for (Integer var : instance.vars) {
            return var;
        }
        throw new NoVariableFoundException("No variable found in SATInstance: " + instance);
    }
}
