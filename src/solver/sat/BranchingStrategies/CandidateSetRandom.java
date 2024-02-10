package solver.sat.BranchingStrategies;

import solver.sat.NoVariableFoundException;
import solver.sat.SATInstance;

import java.util.Map;
import java.util.Random;

public class CandidateSetRandom implements BranchingStrategy{

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

        Random rand = new Random();
        return strategyLiterals.get(rand.nextInt(4));
    }
}
