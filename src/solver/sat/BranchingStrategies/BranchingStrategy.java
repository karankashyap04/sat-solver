package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.*;

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

    static int[] getShortestSampleIndices(SATInstance instance, Map<Integer, List<Integer>> clausesOfSize) {
        int totalSampled = 0;
        int sampleSize = Math.min(Math.max(100, instance.clauses.size() / 10), instance.clauses.size());
        int[] sampleIndices = new int[sampleSize];
        int sampleIndex = 0;

        outer: for (int i = 1; i < clausesOfSize.size(); i++) {
            if (totalSampled == sampleSize) break;

            for (Integer ind : clausesOfSize.getOrDefault(i, new ArrayList<>())) {
                sampleIndices[sampleIndex] = ind;
                sampleIndex ++;
                totalSampled ++;
                if (totalSampled == sampleSize) break outer;
            }
        }

        return sampleIndices;
    }
}
