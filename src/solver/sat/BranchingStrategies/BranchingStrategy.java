package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public interface BranchingStrategy {

    Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException;

    static int[] getSampleIndices(SATInstance instance) {
        int sampleSize = Math.min(Math.max(100, instance.clauses.size() / 10), instance.clauses.size());
        int[] sampleIndices = new int[sampleSize];
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
        for (int i = 0; i < sampleSize; i++) {
            sampleIndices[i] = i;
        }
        return sampleIndices;
    }
}
