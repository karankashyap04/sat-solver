package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MamsSampled implements BranchingStrategy {

    private int[] sampleIndices;

    @Override
    public void setRemainingClauses(Set<Integer> remainingClauses) {
    }

    public MamsSampled(int[] sampleIndices) {
        this.sampleIndices = sampleIndices;
    }

    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Map<Integer, Integer> literalScores = new HashMap<>();
        int maxVar = 0;
        int maxScore = Integer.MIN_VALUE;

        // MAXO
//        for (Set<Integer> clause : instance.clauses) { // NOTE: clauses shouldn't be empty
        for (int sampleIdx : this.sampleIndices) {
            Set<Integer> clause = instance.clauses.get(sampleIdx);
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
