package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Checks depth of 1 for unit propagations; selects literal leading to the most unit propagations.
 */
public class AdaptiveDeepSupNShortest implements BranchingStrategy {
    private Set<Integer> remainingClauses;
    private Map<Integer, Set<Integer>> globalRemovedLiterals;

    public void setContext(Set<Integer> remainingClauses, Map<Integer, Set<Integer>> globalRemovedLiterals) {
        this.remainingClauses = remainingClauses;
        this.globalRemovedLiterals = globalRemovedLiterals;
    }

    private int UP(SATInstance instance, Set<Integer> toUnitPropagate, Map<Integer, Integer> clauseLiteralRemoveCount, Set<Integer> removedLiterals) {
        Set<Integer> nextUnitPropagations = new HashSet<>();
        for (Integer i : this.remainingClauses) {
            Set<Integer> clause = instance.clauses.get(i);
            Set<Integer> clauseGlobalRemovedLiterals = this.globalRemovedLiterals.getOrDefault(i, new HashSet<>());
            int clauseSize = clause.size() - clauseGlobalRemovedLiterals.size() - clauseLiteralRemoveCount.getOrDefault(i, 0);
            if (clauseSize == 0) {
                continue;
            }
            if (clauseSize < 0) {
//                System.out.println("Negative clause size.");
//                System.out.println("Removed: " + removedLiterals + ", Clause: " + instance.clauses.get(i));
                break;
            }
            if (clauseSize == 1) {
//                System.out.println("size is 1 error");
                for (Integer literal : clause) {
                    if (
                            !toUnitPropagate.contains(literal)
                                    && !toUnitPropagate.contains(-literal)
                                    && !removedLiterals.contains(literal)
                                    && !removedLiterals.contains(-literal)
                                    && !clauseGlobalRemovedLiterals.contains(literal)) {
                        // throw error
                        System.out.println("size is 1 error");
                        break;
                    }
                }
//                continue;
            }
            // go through things to be unit propagated. if contained here, remove this clause
            for (Integer literal : toUnitPropagate) {
                if (instance.clauses.get(i).contains(literal) && !clauseGlobalRemovedLiterals.contains(literal)) {
                    // remove this clause
                    clauseLiteralRemoveCount.put(i, instance.clauses.get(i).size());
                    break;
                }
                if (instance.clauses.get(i).contains(-literal) && !clauseGlobalRemovedLiterals.contains(-literal)) {
                    // increment remove count by 1
                    clauseLiteralRemoveCount.put(i, 1 + clauseLiteralRemoveCount.getOrDefault(i, 0));
                }
            }

            // now, all unit propagations on this clause are done. so, if the new clause size is 1, add
            // to nextUnitPropagations
            if (clause.size() -  clauseGlobalRemovedLiterals.size() - clauseLiteralRemoveCount.getOrDefault(i, 0) == 1) {
                for (Integer literal : clause) {
                    if (!clauseGlobalRemovedLiterals.contains(literal))
                        continue;
                    if (!removedLiterals.contains(literal) && !removedLiterals.contains(-literal)) {
                        nextUnitPropagations.add(literal);
                        removedLiterals.add(literal);
                        break;
                    }
                }
            }
        }

        return nextUnitPropagations.size();
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (this.remainingClauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

//        Map<Integer, List<Integer>> clausesOfSize = BranchingStrategy.getSizeIndices(instance);
//        int[] sampleIndices = BranchingStrategy.getShortestSampleIndices(instance, clausesOfSize);

        //MAXO
        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);

        // MOMS
        MaxOccurrencesMinSize moms = new MaxOccurrencesMinSize();
        moms.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Integer momsLiteral = moms.pickBranchingVariable(instance);
//        Integer momsLiteral = new MOMSIndices(clausesOfSize).pickBranchingVariable(instance);
//        Integer mamsLiteral = new MamsSampled(sampleIndices).pickBranchingVariable(instance);
//        Integer jwLiteral = new JeroslawWangSampled(sampleIndices).pickBranchingVariable(instance);

        int MAXO = 0, MOMS = 1; // constants for strategies
        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral);
        int[]deepUpScores = new int[2];
        for (int i = 0; i < 2; i++) {
            Set<Integer> toUnitPropagate = new HashSet<>();
            toUnitPropagate.add(strategyLiterals.get(i));

            Set<Integer> removedLiterals = new HashSet<>();
            removedLiterals.add(strategyLiterals.get(i));

            // adapt depth based on average expression length
            deepUpScores[i] = UP(instance, toUnitPropagate, new HashMap<>(), removedLiterals);
        }

        int argmax = 0;
        for (int i = 1; i < 2; i++) {
            if (deepUpScores[i] > deepUpScores[argmax]) {
                argmax = i;
            }
        }
        return strategyLiterals.get(argmax);
    }
}