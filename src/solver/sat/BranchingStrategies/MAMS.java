package solver.sat.BranchingStrategies;

import java.util.*;

/**
 * Implements the MAMS branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

public class MAMS implements BranchingStrategy {
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

        Map<Integer, Integer> literalScores = new HashMap<>();
        int maxVar = 0;
        int maxScore = Integer.MIN_VALUE;

        // MAXO
//        for (Set<Integer> clause : instance.clauses) { // NOTE: clauses shouldn't be empty
        for (Integer clauseIdx : this.remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            Set<Integer> clauseGlobalRemovedLiterals = this.globalRemovedLiterals.getOrDefault(clauseIdx, new HashSet<>());
            for (Integer literal : clause) {
                if (clauseGlobalRemovedLiterals.contains(literal))
                    continue;
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
        MaxOccurrencesMinSize moms = new MaxOccurrencesMinSize();
        moms.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Set<Integer> minSizeClauses = moms.getMinSizeClauses(instance);
//        for (Set<Integer> clause : minSizeClauses) {
        for (Integer clauseIdx : minSizeClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            Set<Integer> clauseGlobalRemovedLiterals = this.globalRemovedLiterals.getOrDefault(clauseIdx, new HashSet<>());
            for (Integer literal : clause) {
                if (clauseGlobalRemovedLiterals.contains(literal))
                    continue;
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
