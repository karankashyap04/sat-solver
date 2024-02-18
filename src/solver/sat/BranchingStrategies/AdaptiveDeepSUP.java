//package solver.sat.BranchingStrategies;
//
//import solver.sat.NoVariableFoundException;
//import solver.sat.SATInstance;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import java.math.*;
//
//public class AdaptiveDeepSUP implements BranchingStrategy {
//    private Set<Integer> remainingClauses;
//    private Map<Integer, Set<Integer>> globalRemovedLiterals;
//
//    public void setContext(Set<Integer> remainingClauses, Map<Integer, Set<Integer>> globalRemovedLiterals) {
//        this.remainingClauses = remainingClauses;
//        this.globalRemovedLiterals = globalRemovedLiterals;
//    }
//
//    private int UP(int depth, SATInstance instance, Set<Integer> toUnitPropagate, Map<Integer, Integer> clauseLiteralRemoveCount, Set<Integer> removedLiterals) {
//        if (depth == 0 || toUnitPropagate.isEmpty()) {
//            return 0;
//        }
//        Set<Integer> nextUnitPropagations = new HashSet<>();
//        for (int i = 0; i < instance.clauses.size(); i++) {
//            int clauseSize = instance.clauses.get(i).size() - clauseLiteralRemoveCount.getOrDefault(i, 0);
//            if (clauseSize == 0) {
//                continue;
//            }
//            if (clauseSize < 0) {
////                System.out.println("Negative clause size.");
////                System.out.println("Removed: " + removedLiterals + ", Clause: " + instance.clauses.get(i));
//                break;
//            }
//            if (clauseSize == 1) {
////                System.out.println("size is 1 error");
//                for (Integer literal : instance.clauses.get(i)) {
//                    if (
//                            !toUnitPropagate.contains(literal)
//                                    && !toUnitPropagate.contains(-literal)
//                                    && !removedLiterals.contains(literal)
//                                    && !removedLiterals.contains(-literal)) {
//                        // throw error
////                        System.out.println("Removed literals: " + removedLiterals + ", Literal: " + literal);
//
////                        System.out.println("Unit propagation literal not in toUnitPropagate");
////                        System.out.println("Removed: " + removedLiterals + ", Clause: " + instance.clauses.get(i));
////                        nextUnitPropagations.add(literal);
////                        System.out.println("removed");
//                        break;
//                    }
//                }
////                continue;
//            }
//            // go through things to be unit propagated. if contained here, remove this clause
//            for (Integer literal : toUnitPropagate) {
//                if (instance.clauses.get(i).contains(literal)) {
//                    // remove this clause
//                    clauseLiteralRemoveCount.put(i, instance.clauses.get(i).size());
//                    break;
//                }
//                if (instance.clauses.get(i).contains(-literal)) {
//                    // increment remove count by 1
//                    clauseLiteralRemoveCount.put(i, 1 + clauseLiteralRemoveCount.getOrDefault(i, 0));
//                }
//            }
//
//            // now, all unit propagations on this clause are done. so, if the new clause size is 1, add
//            // to nextUnitPropagations
//            if (instance.clauses.get(i).size() - clauseLiteralRemoveCount.getOrDefault(i, 0) == 1) {
//                for (Integer literal : instance.clauses.get(i)) {
//                    if (!removedLiterals.contains(literal) && !removedLiterals.contains(-literal)) {
//                        nextUnitPropagations.add(literal);
//                        removedLiterals.add(literal);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return nextUnitPropagations.size() + UP(depth-1, instance, nextUnitPropagations, clauseLiteralRemoveCount, removedLiterals);
//    }
//
//    @Override
//    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
//        if (instance.clauses.isEmpty()) {
//            // pickBranchingVariable should never be called if this is the case (already SAT!)
//            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
//        }
//
//        int[] sampleIndices = BranchingStrategy.getSampleIndices(instance);
//
//        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);
//        Integer momsLiteral = new MaxOccurrencesMinSize().pickBranchingVariable(instance);
////        Integer mamsLiteral = new MamsSampled(sampleIndices).pickBranchingVariable(instance);
////        Integer jwLiteral = new JeroslawWangSampled(sampleIndices).pickBranchingVariable(instance);
//
////        int MAXO = 0, MOMS = 1, MAMS = 2, JW = 3; // constants for strategies
//        int MAXO = 0, MOMS = 1;
////        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral, MAMS, mamsLiteral, JW, jwLiteral);
//        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral);
//        int[]deepUpScores = new int[2];
//        for (int i = 0; i < 2; i++) {
//            Set<Integer> toUnitPropagate = new HashSet<>();
//            toUnitPropagate.add(strategyLiterals.get(i));
//
//            Set<Integer> removedLiterals = new HashSet<>();
//            removedLiterals.add(strategyLiterals.get(i));
//
//            double expressionLength = 0;
//            for (Set<Integer> clause : instance.clauses) {
//                expressionLength += clause.size();
//            }
//
//            // adapt depth based on average expression length
//            deepUpScores[i] = UP(1, instance, toUnitPropagate, new HashMap<>(), removedLiterals);
//        }
//
//        int argmax = 0;
//        for (int i = 1; i < 2; i++) {
//            if (deepUpScores[i] > deepUpScores[argmax]) {
//                argmax = i;
//            }
//        }
//        return strategyLiterals.get(argmax);
//    }
//}

package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.math.*;

public class AdaptiveDeepSUP implements BranchingStrategy {

    private Set<Integer> remainingClauses;
    private Map<Integer, Set<Integer>> globalRemovedLiterals;

    public void setContext(Set<Integer> remainingClauses, Map<Integer, Set<Integer>> globalRemovedLiterals) {
        this.remainingClauses = remainingClauses;
        this.globalRemovedLiterals = globalRemovedLiterals;
    }

    private int UP(int depth, SATInstance instance, Set<Integer> toUnitPropagate, Map<Integer, Integer> clauseLiteralRemoveCount, Set<Integer> removedLiterals) {
        if (depth == 0 || toUnitPropagate.isEmpty()) {
            return 0;
        }
        Set<Integer> nextUnitPropagations = new HashSet<>();
//        for (int i = 0; i < instance.clauses.size(); i++) {
        for (Integer clauseIdx : this.remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            Set<Integer> clauseGlobalRemovedLiterals = this.globalRemovedLiterals.getOrDefault(clauseIdx, new HashSet<>());
            int clauseSize = clause.size() - clauseGlobalRemovedLiterals.size() - clauseLiteralRemoveCount.getOrDefault(clauseIdx, 0);
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
                    if (clauseGlobalRemovedLiterals.contains(literal))
                        continue;
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
                if (clause.contains(literal)) {
                    // remove this clause
                    clauseLiteralRemoveCount.put(clauseIdx, clause.size() - clauseGlobalRemovedLiterals.size());
                    break;
                }
                if (clause.contains(-literal)) {
                    if (clauseGlobalRemovedLiterals.contains(-literal))
                        continue;
                    // increment remove count by 1
                    clauseLiteralRemoveCount.put(clauseIdx, 1 + clauseLiteralRemoveCount.getOrDefault(clauseIdx, 0));
                }
            }

            // now, all unit propagations on this clause are done. so, if the new clause size is 1, add
            // to nextUnitPropagations
            if (clause.size() - clauseGlobalRemovedLiterals.size() - clauseLiteralRemoveCount.getOrDefault(clauseIdx, 0) == 1) {
                for (Integer literal : clause) {
                    if (clauseGlobalRemovedLiterals.contains(literal))
                        continue;
                    if (!removedLiterals.contains(literal) && !removedLiterals.contains(-literal)) {
                        nextUnitPropagations.add(literal);
                        removedLiterals.add(literal);
                        break;
                    }
                }
            }
        }

        return nextUnitPropagations.size() + UP(depth - 1, instance, nextUnitPropagations, clauseLiteralRemoveCount, removedLiterals);
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (this.remainingClauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

//        int[] sampleIndices = BranchingStrategy.getSampleIndices(instance);

        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);

        MaxOccurrencesMinSize moms = new MaxOccurrencesMinSize();
        moms.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Integer momsLiteral = moms.pickBranchingVariable(instance);

        MAMS mams = new MAMS();
        mams.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Integer mamsLiteral = mams.pickBranchingVariable(instance);

        JeroslawWang jw = new JeroslawWang();
        jw.setContext(this.remainingClauses, this.globalRemovedLiterals);
        Integer jwLiteral = new JeroslawWang().pickBranchingVariable(instance);

        int MAXO = 0, MOMS = 1, MAMS = 2, JW = 3; // constants for strategies
        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral, MAMS, mamsLiteral, JW, jwLiteral);
        int[] deepUpScores = new int[4];
        for (int i = 0; i < 4; i++) {
            Set<Integer> toUnitPropagate = new HashSet<>();
            toUnitPropagate.add(strategyLiterals.get(i));

            Set<Integer> removedLiterals = new HashSet<>();
            removedLiterals.add(strategyLiterals.get(i));

            double expressionLength = 0;
            for (Set<Integer> clause : instance.clauses) {
                expressionLength += clause.size();
            }

            // adapt depth based on average expression length
            deepUpScores[i] = UP((int) (expressionLength / (2 * Double.valueOf(instance.getNumClauses()))), instance, toUnitPropagate, new HashMap<>(), removedLiterals);
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
