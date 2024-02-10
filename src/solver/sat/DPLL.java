package solver.sat;

import solver.sat.BranchingStrategies.BranchingStrategy;

import java.util.*;

public class DPLL {

    BranchingStrategy branchingStrategy;
    List<Integer> currentUnitClauses;

    public DPLL(BranchingStrategy branchingStrategy) {
        this.branchingStrategy = branchingStrategy;
        this.currentUnitClauses = new ArrayList<>();
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
            boolean keepClause = true;
            for (Integer pureSymbol : pureSymbols) {
                if (clause.contains(pureSymbol)) {
                    instance.numClauses--;
                    keepClause = false;
                    break;
                }
            }
            if (keepClause) {
                updatedClauses.add(clause);
            }
        }
        model.model.addAll(pureSymbols); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
        for (Integer pureSymbol : pureSymbols) {
            instance.numVars --;
            instance.vars.remove((pureSymbol < 0) ? -1 * pureSymbol : pureSymbol);
        }
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

    private List<Integer> findUnitClauses(SATInstance instance) {
        // NOTE: this is meant to just be called once at the beginning to find the initial unit clauses
        List<Integer> unitClauses = new ArrayList<>();
        for (Set<Integer> clause : instance.clauses) {
            if (clause.size() == 1) {
                for (Integer literal : clause) {
                    unitClauses.add(literal);
                    break;
                }
            }
        }
        return unitClauses;
    }

    private void propagateUnitClause(SATInstance instance, Integer literal, Model model) {
        List<Set<Integer>> updatedClauses = new ArrayList<>();
        for (Set<Integer> clause : instance.clauses) {
            if (clause.contains(-literal)) {
                clause.remove(-literal);
                updatedClauses.add(clause);
                if (clause.size() == 1) {
                    for (Integer unitSymbol : clause) {
                        this.currentUnitClauses.add(unitSymbol);
                        break;
                    }
                }
            }
            else if (!clause.contains(literal)) {
                updatedClauses.add(clause);
            }
        }
        model.model.add(literal); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
        instance.numClauses = updatedClauses.size();
        instance.vars.remove(literal < 0 ? -literal : literal);
        instance.numVars--;
    }

//    private Integer pickBranchVariable(SATInstance instance) throws NoVariableFoundException {
//        // TODO: make smarter search heuristics at some point
//        for (Integer var : instance.vars) {
//            return var;
//        }
//        throw new NoVariableFoundException("No variable found in SATInstance: " + instance);
//    }

    private boolean hasEmptyClause(SATInstance instance) {
        for (Set<Integer> clause : instance.clauses) {
            if (clause.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSAT(SATInstance instance) {
        // checks if guaranteed to be sat already
        return instance.clauses.size() == 0;
    }

    private DPLLResult dpllInternal(SATInstance instance, Model model) {
        if (isSAT(instance)) {
            return new DPLLResult(instance, model, true);
        }

        if (hasEmptyClause(instance)) {
            return new DPLLResult(instance, model, false);
        }

        boolean propagatedUnitClause = false;
        while (!this.currentUnitClauses.isEmpty()) {
            Integer unitSymbol = this.currentUnitClauses.remove(this.currentUnitClauses.size() - 1);
            this.propagateUnitClause(instance, unitSymbol, model);
            propagatedUnitClause = true;
        }
        if (propagatedUnitClause) {
            return dpllInternal(instance, model);
        }

        // TODO: mess with order of pure/unit symbols
        Set<Integer> pureSymbols = findPureSymbols(instance);
        if (!pureSymbols.isEmpty()) {
            propagatePureSymbols(pureSymbols, instance, model);
            return dpllInternal(instance, model);
        }

//        Integer unitSymbol = findUnitClause(instance);
//        if (unitSymbol != 0) {
//            propagateUnitClause(instance, unitSymbol, model);
//            return dpll(instance,model);
//        }

        try {
            Integer branchVariable = this.branchingStrategy.pickBranchingVariable(instance);

            // positive assumption
            SATInstance positiveInstance = instance.copy();
            Model positiveModel = model.copy();
            Set<Integer> positiveUnitClause = new HashSet<>();
            positiveUnitClause.add(branchVariable);
            positiveInstance.addClause(positiveUnitClause);
            positiveInstance.numClauses ++;
            propagateUnitClause(positiveInstance, branchVariable, positiveModel);

            DPLLResult positiveAssumptionResult = dpll(positiveInstance, positiveModel);
            if (positiveAssumptionResult.isSAT) {
                return positiveAssumptionResult;
            }

            // negative assumption
            SATInstance negativeInstance = instance.copy();
            Model negativeModel = model.copy();
            Set<Integer> negativeUnitClause = new HashSet<>();
            negativeUnitClause.add(-1 * branchVariable);
            negativeInstance.addClause(negativeUnitClause);
            negativeInstance.numClauses ++;
            propagateUnitClause(negativeInstance, -1 * branchVariable, negativeModel);

            return dpllInternal(negativeInstance, negativeModel);
        }
        catch (NoVariableFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    public DPLLResult dpll(SATInstance instance, Model model) {
        this.currentUnitClauses = this.findUnitClauses(instance);
        return this.dpllInternal(instance, model);
    }
}
