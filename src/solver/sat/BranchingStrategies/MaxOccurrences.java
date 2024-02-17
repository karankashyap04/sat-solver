package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implements the MAXO branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
//public class MaxOccurrences implements BranchingStrategy {
//
//    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
//        if (instance.clauses.isEmpty()) {
//            // pickBranchingVariable should never be called if this is the case (already SAT!)
//            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
//        }
//
//        Map<Integer, Integer> literalScores = new HashMap<>();
//        int maxOccurrenceVar = 0;
//        int maxOccurrenceVarScore = 0;
//        for (Set<Integer> clause : instance.clauses) { // NOTE: clauses shouldn't be empty
//            for (Integer literal : clause) {
//                literalScores.put(literal, 1 + literalScores.getOrDefault(literal, 0));
//                int var = literal < 0 ? -literal : literal; // variable for this literal
//                int varScore = literalScores.getOrDefault(literal, 0) + literalScores.getOrDefault(-literal, 0);
//                if (maxOccurrenceVar == 0 || varScore > maxOccurrenceVarScore) {
//                    maxOccurrenceVar = var;
//                    maxOccurrenceVarScore = varScore;
//                }
//            }
//        }
//
//        // want to return the literal for this variable with the lower score (var vs. -var)
//        if (literalScores.getOrDefault(maxOccurrenceVar, 0) >= literalScores.getOrDefault(-maxOccurrenceVar, 0)) {
//            return maxOccurrenceVar;
//        }
//        return -maxOccurrenceVar;
//    }
//}


public class MaxOccurrences implements BranchingStrategy {

    @Override
    public void setRemainingClauses(Set<Integer> remainingClauses) {
    }
//    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
//        if (instance.clauses.isEmpty()) {
//            // pickBranchingVariable should never be called if this is the case (already SAT!)
//            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
//        }
//
//        int maxOccurrenceVar = 0;
//        int maxOccurrenceVarScore = 0;
//
//        for (Integer literal : instance.literalCounts.keySet()) {
//            int score = instance.literalCounts.getOrDefault(literal, 0) + instance.literalCounts.getOrDefault(-literal, 0);
//            if (maxOccurrenceVarScore == 0 || score > maxOccurrenceVarScore) {
//                maxOccurrenceVar = literal < 0 ? -literal : literal;
//                maxOccurrenceVarScore = score;
//            }
//        }
//
//        // want to return the literal for this variable with the lower score (var vs. -var)
//        if (instance.literalCounts.getOrDefault(maxOccurrenceVar, 0) >= instance.literalCounts.getOrDefault(-maxOccurrenceVar, 0)) {
//            return maxOccurrenceVar;
//        }
//        return -maxOccurrenceVar;
//    }
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty() || instance.sortedVarCounts.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Set<Integer> maxOccurrenceVars = instance.sortedVarCounts.get(instance.sortedVarCounts.firstKey());
        if (maxOccurrenceVars.isEmpty()) {
            System.out.println("empty set included in tree map!");
            return -1;
        }
        int maxOccurrenceVar = -1;
        for (Integer var : maxOccurrenceVars) {
            maxOccurrenceVar = var;
            break;
        }

        // want to return the literal for this variable with the lower score (var vs. -var)
        if (instance.literalCounts.getOrDefault(maxOccurrenceVar, 0) >= instance.literalCounts.getOrDefault(-maxOccurrenceVar, 0)) {
            return maxOccurrenceVar;
        }
        return -maxOccurrenceVar;
    }
}