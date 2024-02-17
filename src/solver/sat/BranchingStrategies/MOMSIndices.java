package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;

public class MOMSIndices implements BranchingStrategy {
    Map<Integer, List<Integer>> clausesOfSize;

    @Override
    public void setRemainingClauses(Set<Integer> remainingClauses) {

    }

    public MOMSIndices(Map<Integer, List<Integer>> clausesOfSize) {
        this.clausesOfSize = clausesOfSize;
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }
        int currMinSize = Integer.MAX_VALUE;
        for (Integer size : clausesOfSize.keySet()) {
            if (size < currMinSize) {
                currMinSize = size;
            }
        }
        List<Integer> minClauseIndices = clausesOfSize.get(currMinSize);
        List<Set<Integer>> minSizeClauses = new ArrayList<>();
        for (int index : minClauseIndices) {
            minSizeClauses.add(instance.clauses.get(index));
        }

        Map<Integer, Integer> literalScores = new HashMap<>();
        int maxOccurrences = Integer.MIN_VALUE; // max occurrence var
        int maxVar = 0;

        for (Set<Integer> clause : minSizeClauses) {
            for (Integer literal : clause) {
                literalScores.put(literal, 1 + literalScores.getOrDefault(literal, 0));
                int var = literal < 0 ? -literal : literal;
                int varScore = literalScores.getOrDefault(literal, 0) + literalScores.getOrDefault(-literal,0);
                if (varScore > maxOccurrences) {
                    maxOccurrences = varScore;
                    maxVar = var;
                }
            }
        }

        if (literalScores.getOrDefault(maxVar, 0) >= literalScores.getOrDefault(-maxVar, 0)) {
            return maxVar;
        }
        return -maxVar;
    }
}
