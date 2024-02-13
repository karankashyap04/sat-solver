package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class AdaptiveDeepSupNShortest implements BranchingStrategy {
    private int UP(SATInstance instance, Set<Integer> toUnitPropagate, Map<Integer, Integer> clauseLiteralRemoveCount, Set<Integer> removedLiterals) {
        Set<Integer> nextUnitPropagations = new HashSet<>();
        for (int i = 0; i < instance.clauses.size(); i++) {
            int clauseSize = instance.clauses.get(i).size() - clauseLiteralRemoveCount.getOrDefault(i, 0);
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
                for (Integer literal : instance.clauses.get(i)) {
                    if (
                            !toUnitPropagate.contains(literal)
                                    && !toUnitPropagate.contains(-literal)
                                    && !removedLiterals.contains(literal)
                                    && !removedLiterals.contains(-literal)) {
                        // throw error
//                        System.out.println("Removed literals: " + removedLiterals + ", Literal: " + literal);

//                        System.out.println("Unit propagation literal not in toUnitPropagate");
//                        System.out.println("Removed: " + removedLiterals + ", Clause: " + instance.clauses.get(i));
//                        nextUnitPropagations.add(literal);
//                        System.out.println("removed");
                        break;
                    }
                }
//                continue;
            }
            // go through things to be unit propagated. if contained here, remove this clause
            for (Integer literal : toUnitPropagate) {
                if (instance.clauses.get(i).contains(literal)) {
                    // remove this clause
                    clauseLiteralRemoveCount.put(i, instance.clauses.get(i).size());
                    break;
                }
                if (instance.clauses.get(i).contains(-literal)) {
                    // increment remove count by 1
                    clauseLiteralRemoveCount.put(i, 1 + clauseLiteralRemoveCount.getOrDefault(i, 0));
                }
            }

            // now, all unit propagations on this clause are done. so, if the new clause size is 1, add
            // to nextUnitPropagations
            if (instance.clauses.get(i).size() - clauseLiteralRemoveCount.getOrDefault(i, 0) == 1) {
                for (Integer literal : instance.clauses.get(i)) {
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
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Map<Integer, List<Integer>> clausesOfSize = MOMSIndices.getSizeIndices(instance);
        int[] sampleIndices = BranchingStrategy.getShortestSampleIndices(instance, clausesOfSize);

        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);
        Integer momsLiteral = new MOMSIndices(clausesOfSize).pickBranchingVariable(instance);
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

            double expressionLength = 0;
            for (Set<Integer> clause : instance.clauses) {
                expressionLength += clause.size();
            }

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
