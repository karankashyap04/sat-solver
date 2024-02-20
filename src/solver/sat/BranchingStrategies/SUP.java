package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * Implements the SUP branching strategy as per Lagoudakis, M.
 * & Littman, M.
 * (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
 */
public class SUP implements BranchingStrategy{
    private Set<Integer> remainingClauses;
    private Map<Integer, Set<Integer>> globalRemovedLiterals;

    public void setContext(Set<Integer> remainingClauses, Map<Integer, Set<Integer>> globalRemovedLiterals) {
        this.remainingClauses = remainingClauses;
        this.globalRemovedLiterals = globalRemovedLiterals;
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (this.remainingClauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

//        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);
//        Integer momsLiteral = new MaxOccurrencesMinSize().pickBranchingVariable(instance);
        MaxOccurrences maxo = new MaxOccurrences();
        maxo.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Integer maxoLiteral = maxo.pickBranchingVariable(instance);

        MaxOccurrencesMinSize moms = new MaxOccurrencesMinSize();
        moms.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Integer momsLiteral = moms.pickBranchingVariable(instance);
//        Integer mamsLiteral = new MAMS().pickBranchingVariable(instance);
//        Integer jwLiteral = new JeroslawWang().pickBranchingVariable(instance);

        int MAXO = 0, MOMS = 1; // constants for strategies
        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral);

        int maxUP = Integer.MIN_VALUE;
        int bestStrategy = MAXO;
        
        int maxoUP = 0, momsUP = 0;
        // Give priority to JW and then MAXO when ties occur
        for (Integer clauseIdx : this.remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            Set<Integer> clauseRemovedLiterals = this.globalRemovedLiterals.getOrDefault(clauseIdx, new HashSet<>());
            if (clause.size() - clauseRemovedLiterals.size() == 2) {
                // NOTE: it is faster if you incorrect check if the literal (not it's negation) is what is present
                if (clause.contains(-maxoLiteral) && !clause.contains(maxoLiteral) && !clauseRemovedLiterals.contains(-maxoLiteral)) {
                    maxoUP++;
                    if (maxoUP >= maxUP) {
                        bestStrategy = MAXO;
                        maxUP = maxoUP;
                    }
                }
                // NOTE: it is faster if you incorrect check if the literal (not it's negation) is what is present
                if (clause.contains(-momsLiteral) && !clause.contains(momsLiteral) && !clauseRemovedLiterals.contains(-momsLiteral)) {
                    momsUP++;
                    if (momsUP > maxUP) {
                        bestStrategy = MOMS;
                        maxUP = momsUP;
                    }
                }
//                if (clause.contains(mamsLiteral)) {
//                    mamsUP++;
//                    if (mamsUP > maxUP) {
//                        bestStrategy = MAMS;
//                        maxUP = mamsUP;
//                    }
//                }
//                if (clause.contains(jwLiteral)) {
//                    jwUP++;
//                    if (jwUP >= maxUP) {
//                        bestStrategy = JW;
//                        maxUP = jwUP;
//                    }
//                }
            }
        }

        return strategyLiterals.get(bestStrategy);
    }

}
