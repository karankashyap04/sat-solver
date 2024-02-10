package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class JeroslawWangSampled implements BranchingStrategy{

    private int[] sampleIndices;

    public JeroslawWangSampled(int[] sampleIndices) {
        this.sampleIndices = sampleIndices;
    }

    @Override
    public Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException {
        if (instance.clauses.isEmpty()) {
            // pickBranchingVariable should never be called if this is the case (already SAT!)
            throw new NoVariableFoundException("tried to pick branching var with no clauses - already SAT");
        }

//        // random indices sample
//        int sampleSize = Math.min(Math.max(instance.clauses.size(), instance.clauses.size() / 10), instance.clauses.size());
//        int[] sampleIndices = new int[sampleSize];
//        if (sampleSize == instance.clauses.size()) {
//            for (int i = 0; i < sampleSize; i++) {
//                sampleIndices[i] = i;
//            }
//        } else {
//            Set<Integer> indices = new HashSet<>();
//            Random random = new Random();
//            while (indices.size() < sampleSize) {
//                indices.add(random.nextInt(sampleSize));
//            }
//            int i = 0;
//            for (Integer sampleIdx : indices) {
//                sampleIndices[i] = sampleIdx;
//                i++;
//            }
//        }

        Map<Integer, Double> literalScores = new HashMap<>();
        int maxVar = 0;
        double maxScore = Double.MIN_VALUE;

//        for (Set<Integer> clause : instance.clauses) { // NOTE: clauses shouldn't be empty
        for (int sampleIdx : this.sampleIndices) {
            Set<Integer> clause = instance.clauses.get(sampleIdx);
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
