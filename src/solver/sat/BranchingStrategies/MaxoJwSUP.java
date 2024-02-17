package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.Map;
import java.util.Set;

/**
 * Implements a modified SUP branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
public class MaxoJwSUP implements BranchingStrategy {
    @Override
    public void setRemainingClauses(Set<Integer> remainingClauses) {
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);
        Integer jwLiteral = new JeroslawWang().pickBranchingVariable(instance);

        int MAXO = 0, JW = 1; // constants for strategies
        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, JW, jwLiteral);

        int maxUP = Integer.MIN_VALUE;
        int bestStrategy = JW;

        int maxoUP = 0, jwUP = 0;
        // Give priority to JW and then MAXO when ties occur
        for (Set<Integer> clause : instance.clauses) {
            if (clause.size() == 2) {
                if (clause.contains(maxoLiteral)) {
                    maxoUP++;
                    if (maxoUP >= maxUP) {
                        bestStrategy = MAXO;
                        maxUP = maxoUP;
                    }
                }
                if (clause.contains(jwLiteral)) {
                    jwUP++;
                    if (jwUP >= maxUP) {
                        bestStrategy = JW;
                        maxUP = jwUP;
                    }
                }
            }
        }

        return strategyLiterals.get(bestStrategy);
    }
}
