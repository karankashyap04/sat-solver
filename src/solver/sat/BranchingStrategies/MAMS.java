package solver.sat.BranchingStrategies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements the MAMS branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

public class MAMS implements BranchingStrategy {
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Map<Integer, Integer> literalScores = new HashMap<>();
        int maxVar = 0;
        int maxScore = Integer.MIN_VALUE;

        // MAXO
        for (Set<Integer> clause : instance.clauses) { // NOTE: clauses shouldn't be empty
            for (Integer literal : clause) {
                literalScores.put(literal, 1 + literalScores.getOrDefault(literal, 0));
                int var = literal < 0 ? -literal : literal; // variable for this literal
                int varScore = literalScores.getOrDefault(literal, 0) + literalScores.getOrDefault(-literal, 0);
                if (varScore > maxScore) {
                    maxScore = varScore;
                    maxVar = var;
                }
            }
        }

        // MOMS
        List<Set<Integer>> minSizeClauses = new MaxOccurrencesMinSize().getMinSizeClauses(instance);
        for (Set<Integer> clause : minSizeClauses) {
            for (Integer literal : clause) {
                literalScores.put(literal, 1 + literalScores.getOrDefault(literal, 0));
                int var = literal < 0 ? -literal : literal;
                int varScore = literalScores.getOrDefault(literal, 0) + literalScores.getOrDefault(-literal, 0);
                if (varScore > maxScore) {
                    maxScore = varScore;
                    maxVar = var;
                }
            }
        }

        // get best literal for this var (-var vs var)
        if (literalScores.getOrDefault(maxVar, 0) >= literalScores.getOrDefault(-maxVar, 0)) {
            return maxVar;
        }
        return -maxVar;
    }
}
