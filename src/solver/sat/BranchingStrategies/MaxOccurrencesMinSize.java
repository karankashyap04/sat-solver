package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;

/**
 * Implements the MOMS branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
public class MaxOccurrencesMinSize implements BranchingStrategy{
    
    private List<Set<Integer>> getMinSizeClauses(SATInstance instance) {
        Map<Integer, List<Set<Integer>>> clausesOfSize = new HashMap<>(); // size -> list of clauses
        int currMinSize = Integer.MAX_VALUE;
        for (Set<Integer> clause : instance.clauses) {
            int size = clause.size();
            if (!clausesOfSize.containsKey(size)) {
                clausesOfSize.put(size, new ArrayList<>());
            }
            clausesOfSize.get(size).add(clause);
            if (size < currMinSize) {
                currMinSize = size;
            }
        }

        return clausesOfSize.get(currMinSize);
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        List<Set<Integer>> minSizeClauses = this.getMinSizeClauses(instance);

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
