package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdaptiveDeepSUP implements BranchingStrategy {
    private int UP(int depth, SATInstance instance, Set<Integer> toUnitPropagate, Map<Integer, Integer> clauseLiteralRemoveCount, Set<Integer> removedLiterals) {
        if (depth == 0 || toUnitPropagate.isEmpty()) {
            return 0;
        }
        Set<Integer> nextUnitPropagations = new HashSet<>();
        for (int i = 0; i < instance.clauses.size(); i++) {
            int clauseSize = instance.clauses.get(i).size() - clauseLiteralRemoveCount.getOrDefault(i, 0);
            if (clauseSize == 0) {
                continue;
            }
            if (clauseSize == 1 || clauseSize < 0) {
                for (Integer literal : instance.clauses.get(i)) {
                    if (!toUnitPropagate.contains(literal)) {
                        // throw error
                        System.out.println("Unit propagation literal not in toUnitPropagate OR negative clause size.");
                    }
                    break;
                }
                continue;
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
                    if (!removedLiterals.contains(literal)) {
                        nextUnitPropagations.add(literal);
                        removedLiterals.add(literal);
                        break;
                    }
                }
            }
        }

        return nextUnitPropagations.size() + UP(depth-1, instance, nextUnitPropagations, clauseLiteralRemoveCount, removedLiterals);
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);
        Integer momsLiteral = new MaxOccurrencesMinSize().pickBranchingVariable(instance);
        Integer mamsLiteral = new MAMS().pickBranchingVariable(instance);
        Integer jwLiteral = new JeroslawWang().pickBranchingVariable(instance);

        int MAXO = 0, MOMS = 1, MAMS = 2, JW = 3; // constants for strategies
        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral, MAMS, mamsLiteral, JW, jwLiteral);
        int[]deepUpScores = new int[4];
        for (int i = 0; i < 4; i++) {
            Set<Integer> toUnitPropagate = new HashSet<>();
            toUnitPropagate.add(strategyLiterals.get(i));

            Set<Integer> removedLiterals = new HashSet<>();
            removedLiterals.add(strategyLiterals.get(i));

            deepUpScores[i] = UP(2, instance, toUnitPropagate, new HashMap<>(), removedLiterals);
        }

        int argmax = 0;
        for (int i = 1; i < 4; i++) {
            if (deepUpScores[i] > deepUpScores[argmax]) {
                argmax = i;
            }
        }
        return strategyLiterals.get(argmax);
    }
}
