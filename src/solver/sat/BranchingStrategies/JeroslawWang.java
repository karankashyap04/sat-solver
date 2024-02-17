package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implements the JW branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
public class JeroslawWang implements BranchingStrategy{

    @Override
    public void setRemainingClauses(Set<Integer> remainingClauses) {
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Map<Integer, Double> literalScores = new HashMap<>();
        int maxVar = 0;
        double maxScore = Double.MIN_VALUE;

        for (Set<Integer> clause : instance.clauses) { // NOTE: clauses shouldn't be empty
            int clauseLength = clause.size();
            double weight = Math.pow(2, -clauseLength);
            for (Integer literal : clause) {
                literalScores.put(literal, weight + literalScores.getOrDefault(literal, 0.0));
                int var = literal < 0 ? -literal : literal; // variable for this literal
                double varScore = literalScores.getOrDefault(literal, 0.0) + literalScores.getOrDefault(-literal, 0.0);
                if (varScore > maxScore) {
                    maxScore = varScore;
                    maxVar = var;
                }
            }
        }

        // get best literal for this var (-var vs var)
        if (literalScores.getOrDefault(maxVar, 0.0) >= literalScores.getOrDefault(-maxVar, 0.0)) {
            return maxVar;
        }
        return -maxVar;
    }
}
