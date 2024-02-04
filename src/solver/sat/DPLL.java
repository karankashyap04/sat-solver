package solver.sat;

import java.util.*;

public class DPLL {

    private SATInstance instance;

    public DPLL(SATInstance instance) {
        this.instance = instance;
    }

    private Set<Integer> findPureSymbols(SATInstance instance) {
        // TODO: write this!!
        HashSet<Integer> pureSymbols = new HashSet<>();
        HashSet<Integer> removed = new HashSet<>();

        for (Set<Integer> clause : instance.clauses) {
            for (Integer var : clause) {
                if (removed.contains(var) || removed.contains(-var)) {
                    continue;
                } else if (pureSymbols.contains(-var)) {
                    pureSymbols.remove(-var);
                    removed.add(-var);
                } else pureSymbols.add(var);
            }
        }

        return pureSymbols;
    }

    private void propagatePureSymbols(Set<Integer> pureSymbols, SATInstance instance, Model model) {
        List<Set<Integer>> updatedClauses = new ArrayList<>();
        for (Set<Integer> clause : instance.clauses) {
            for (Integer pureSymbol : pureSymbols) {
                if (!clause.contains(pureSymbol)) {
                    updatedClauses.add(clause);
                }
            }
        }
        model.model.addAll(pureSymbols); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
    }

    private Integer findUnitClause(SATInstance instance) {
        // TODO: maybe find all unit clauses?? and propagate them all??
        // NOTE: returns 0 if no unit clause is found
        for (Set<Integer> clause : instance.clauses) {
            if (clause.size() == 1) {
                for (Integer var : clause) {
                    return var;
                }
            }
        }
        return 0;
    }

    private void propagateUnitClause(SATInstance instance, Integer literal, Model model) {
        List<Set<Integer>> updatedClauses = new ArrayList<>();
        for (Set<Integer> clause : instance.clauses) {
            if (clause.contains(-literal)) {
                clause.remove(-literal);
                updatedClauses.add(clause);
            }
            else if (!clause.contains(literal)) {
                updatedClauses.add(clause);
            }
        }
        model.model.add(literal); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
    }

    private Integer pickBranchVariable(SATInstance instance) throws NoVariableFoundException {
        // TODO: make smarter search heuristics at some point
        for (Integer var : instance.vars) {
            return var;
        }
        throw new NoVariableFoundException("No variable found in SATInstance: " + instance);
    }

    private boolean hasEmptyClause(SATInstance instance) {
        for (Set<Integer> clause : instance.clauses) {
            if (clause.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public SATInstance dpll(SATInstance instance, Model model) {
        // TODO: write this!!
        return null;
    }
}
