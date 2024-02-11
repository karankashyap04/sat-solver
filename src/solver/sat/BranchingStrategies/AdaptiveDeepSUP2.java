package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.*;

public class AdaptiveDeepSUP2 implements BranchingStrategy {
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

        return nextUnitPropagations.size() + UP(depth-1, instance, nextUnitPropagations, clauseLiteralRemoveCount, removedLiterals);
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

        int[] sampleIndices = BranchingStrategy.getSampleIndices(instance);

//        Integer maxoLiteral = new MaxOccurrences().pickBranchingVariable(instance);
        Integer momsLiteral = new MaxOccurrencesMinSize().pickBranchingVariable(instance);
//        Integer mamsLiteral = new MamsSampled(sampleIndices).pickBranchingVariable(instance);
//        Integer jwLiteral = new JeroslawWangSampled(sampleIndices).pickBranchingVariable(instance);


        Map<Integer, Double> literalScores = new HashMap<>();
        int maxOccurrenceVar = 0;
        int jwVar = 0;

        int maxOccurrenceVarScore = 0;
        double jwScore = Double.MIN_VALUE;

        for (int sampleIdx : sampleIndices) {
            Set<Integer> clause = instance.clauses.get(sampleIdx);
            int clauseLength = clause.size();
            double weight = Math.pow(2, -clauseLength); // JW
            for (Integer literal : clause) {
                literalScores.put(literal, weight + literalScores.getOrDefault(literal, 0.0));
                int var = literal < 0 ? -literal : literal; // variable for this literal
                double varScore = literalScores.getOrDefault(literal, 0.0) + literalScores.getOrDefault(-literal, 0.0);
                if (varScore > jwScore) {
                    jwScore = varScore;
                    jwVar = var;
                }
                
                // MAXO
                int score = instance.literalCounts.getOrDefault(literal, 0) + instance.literalCounts.getOrDefault(-literal, 0);
                if (maxOccurrenceVarScore == 0 || score > maxOccurrenceVarScore) {
                    maxOccurrenceVar = literal < 0 ? -literal : literal;
                    maxOccurrenceVarScore = score;
                }
            }
        }

        Integer jwLiteral, maxoLiteral;
        // JW literal
        if (literalScores.getOrDefault(jwVar, 0.0) >= literalScores.getOrDefault(-jwVar, 0.0)) {
            jwLiteral = jwVar;
        }
        else {
            jwLiteral = -jwVar;
        }
        // MAXO literal
        if (instance.literalCounts.getOrDefault(maxOccurrenceVar, 0) >= instance.literalCounts.getOrDefault(-maxOccurrenceVar, 0)) {
            maxoLiteral = maxOccurrenceVar;
        } else {
            maxoLiteral = -maxOccurrenceVar;
        }

        int MAXO = 0, MOMS = 1, JW = 2; // constants for strategies
        Map<Integer, Integer> strategyLiterals = Map.of(MAXO, maxoLiteral, MOMS, momsLiteral,JW, jwLiteral);
        int[]deepUpScores = new int[3];
        for (int i = 0; i < 3; i++) {
            Set<Integer> toUnitPropagate = new HashSet<>();
            toUnitPropagate.add(strategyLiterals.get(i));

            Set<Integer> removedLiterals = new HashSet<>();
            removedLiterals.add(strategyLiterals.get(i));

            double expressionLength = 0;
            for (Set<Integer> clause : instance.clauses) {
                expressionLength += clause.size();
            }

            // adapt depth based on average expression length
            deepUpScores[i] = UP((int)(expressionLength / (2 * Double.valueOf(instance.getNumClauses()))), instance, toUnitPropagate, new HashMap<>(), removedLiterals);
        }

        int argmax = 0;
        for (int i = 1; i < 3; i++) {
            if (deepUpScores[i] > deepUpScores[argmax]) {
                argmax = i;
            }
        }
        return strategyLiterals.get(argmax);
    }
}
