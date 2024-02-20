package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaxoLiteral implements BranchingStrategy {
    private Set<Integer> remainingClauses;
    private Map<Integer, Set<Integer>> globalRemovedLiterals;

    public void setContext(Set<Integer> remainingClauses, Map<Integer, Set<Integer>> globalRemovedLiterals) {
        this.remainingClauses = remainingClauses;
        this.globalRemovedLiterals = globalRemovedLiterals;
    }

    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (this.remainingClauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        int maxOccurrenceLiteral = 0;
        int maxOccurrenceLiteralScore = 0;

        for (Integer literal : instance.literalCounts.keySet()) {
            int score = instance.literalCounts.getOrDefault(literal, 0);
            if (maxOccurrenceLiteralScore == 0 || score > maxOccurrenceLiteralScore) {
                maxOccurrenceLiteral = literal;
                maxOccurrenceLiteralScore = score;
            }
        }

        return maxOccurrenceLiteral;
    }
}
