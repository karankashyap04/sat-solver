package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.*;

public interface BranchingStrategy {

    Integer pickBranchingVariable(SATInstance instance) throws NoVariableFoundException;

    void setRemainingClauses(Set<Integer> remainingClauses);

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

    public static Map<Integer, List<Integer>> getSizeIndices(SATInstance instance) {
        Map<Integer, List<Integer>> clausesOfSize = new HashMap<>(); // size -> list of clauses
        int currMinSize = Integer.MAX_VALUE;
        for (int i = 0; i < instance.clauses.size(); i++) {
            int size = instance.clauses.get(i).size();
            if (!clausesOfSize.containsKey(size)) {
                clausesOfSize.put(size, new ArrayList<>());
            }
            clausesOfSize.get(size).add(i);
            if (size < currMinSize) {
                currMinSize = size;
            }
        }

        return clausesOfSize;
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
