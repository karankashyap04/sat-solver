package solver.sat;

import solver.sat.BranchingStrategies.BranchingStrategy;

import java.util.*;

public class DPLL {

    BranchingStrategy branchingStrategy;

    public DPLL(BranchingStrategy branchingStrategy) {
        this.branchingStrategy = branchingStrategy;
    }

//    private Set<Integer> findPureSymbols(SATInstance instance) {
//        // TODO: write this!!
//        HashSet<Integer> pureSymbols = new HashSet<>();
//        HashSet<Integer> removed = new HashSet<>();
//
//        for (Set<Integer> clause : instance.clauses) {
//            for (Integer var : clause) {
//                if (removed.contains(var) || removed.contains(-var)) {
//                    continue;
//                } else if (pureSymbols.contains(-var)) {
//                    pureSymbols.remove(-var);
//                    removed.add(-var);
//                } else pureSymbols.add(var);
//            }
//        }
//
//        return pureSymbols;
//    }

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
            } else {
                for (Integer literal : clause) {
                    instance.reduceLiteralCount(literal);
                    instance.reduceVarCount(literal);
                }
            }
        }
        model.model.addAll(pureSymbols); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
        for (Integer pureSymbol : pureSymbols) {
            instance.numVars --;
            instance.vars.remove((pureSymbol < 0) ? -1 * pureSymbol : pureSymbol);

            instance.sortedVarCounts.remove(pureSymbol < 0 ? -pureSymbol : pureSymbol);
        }
    }

//    private Integer findUnitClause(SATInstance instance) {
//        // TODO: maybe find all unit clauses?? and propagate them all??
//        // NOTE: returns 0 if no unit clause is found
//        for (Set<Integer> clause : instance.clauses) {
//            if (clause.size() == 1) {
//                for (Integer var : clause) {
//                    return var;
//                }
//            }
//        }
//        return 0;
//    }
    private void findUnitClauses(SATInstance instance) {
        for (Set<Integer> clause : instance.clauses) {
            if (clause.size() == 1) {
                for (Integer literal : clause) {
                    instance.unitClauses.add(literal);
                    break;
                }
            }
        }
    }

    private void propagateUnitClause(SATInstance instance, Integer literal, Model model) throws EmptyClauseFoundException {
        List<Set<Integer>> updatedClauses = new ArrayList<>();
        for (Set<Integer> clause : instance.clauses) {
            if (clause.contains(-literal)) {
                clause.remove(-literal);
                instance.reduceLiteralCount(-literal);
                instance.reduceVarCount(literal);
                if (clause.isEmpty()) {
                    throw new EmptyClauseFoundException("empty clause found!");
                }
                updatedClauses.add(clause);
                if (clause.size() == 1) {
                    for (Integer unitLiteral : clause) {
                        instance.unitClauses.add(unitLiteral);
                        break;
                    }
                }
            }
            else if (!clause.contains(literal)) {
                updatedClauses.add(clause);
            } else {
                for (Integer clauseLiteral : clause) {
                    instance.reduceLiteralCount(clauseLiteral);
                    instance.reduceVarCount(clauseLiteral);
                    if (!instance.literalCounts.containsKey(clauseLiteral) && instance.literalCounts.containsKey(-clauseLiteral)) {
                        instance.pureSymbols.add(-clauseLiteral);
                    }
                }
            }
        }
        model.model.add(literal); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
        instance.numClauses = updatedClauses.size();
        instance.vars.remove(literal < 0 ? -literal : literal);
        instance.sortedVarCounts.remove(literal < 0 ? -literal : literal);
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

    private  DPLLResult dpllInternal(SATInstance instance, Model model) {
//        System.out.println("num clauses: " + instance.clauses.size());
//        System.out.println("num unit clauses: " + instance.unitClauses.size());
//        System.out.println("num pure literals: " + instance.pureSymbols.size());
        if (isSAT(instance)) {
            return new DPLLResult(instance, model, true);
        }

//        if (hasEmptyClause(instance)) {
//            return new DPLLResult(instance, model, false);
//        }

//        // TODO: mess with order of pure/unit symbols
//        Set<Integer> pureSymbols = findPureSymbols(instance);
//        if (!pureSymbols.isEmpty()) {
//            propagatePureSymbols(pureSymbols, instance, model);
//            return dpll(instance, model);
//        }
        if (!instance.pureSymbols.isEmpty()) {
            propagatePureSymbols(instance.pureSymbols, instance, model);
            instance.pureSymbols.clear();
            return dpllInternal(instance, model);
        }

        try {
            if (!instance.unitClauses.isEmpty()) {
                Integer unitLiteral = instance.unitClauses.remove(instance.unitClauses.size() - 1);
                propagateUnitClause(instance, unitLiteral, model);
                return dpllInternal(instance, model);
            }
        } catch (EmptyClauseFoundException e) { // UNSAT
            return new DPLLResult(instance, model, false);
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
            positiveInstance.literalCounts.put(branchVariable, positiveInstance.literalCounts.getOrDefault(branchVariable, 0) + 1);
            positiveInstance.numClauses ++;
            positiveInstance.unitClauses.add(branchVariable);
//            propagateUnitClause(positiveInstance, branchVariable, positiveModel);

            DPLLResult positiveAssumptionResult = dpllInternal(positiveInstance, positiveModel);
            if (positiveAssumptionResult.isSAT) {
                return positiveAssumptionResult;
            }

            // negative assumption
            SATInstance negativeInstance = instance.copy();
            Model negativeModel = model.copy();
            Set<Integer> negativeUnitClause = new HashSet<>();
            negativeUnitClause.add(-1 * branchVariable);
            negativeInstance.addClause(negativeUnitClause);
            negativeInstance.literalCounts.put(-branchVariable, negativeInstance.literalCounts.getOrDefault(-branchVariable, 0) + 1);
            negativeInstance.numClauses ++;
            negativeInstance.unitClauses.add(-branchVariable);
//            propagateUnitClause(negativeInstance, -1 * branchVariable, negativeModel);

            return dpllInternal(negativeInstance, negativeModel);
        }
        catch (NoVariableFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    public DPLLResult dpll(SATInstance instance, Model model) {
        if (hasEmptyClause(instance)) {
            return new DPLLResult(instance, model, false);
        }

        // finding pure symbols
        for (Set<Integer> clause : instance.clauses) {
            for (Integer literal : clause) {
                Integer literalCount = instance.literalCounts.getOrDefault(literal, 0);
                instance.literalCounts.put(literal, literalCount + 1);
                if (instance.literalCounts.containsKey(-literal)) {
                    instance.pureSymbols.remove(literal);
                    instance.pureSymbols.remove(-literal);
                } else {
                    instance.pureSymbols.add(literal);
                }
            }
        }

        findUnitClauses(instance);

        // populate var counts tree map
        for (Integer var : instance.vars) {
            Integer varScore = instance.literalCounts.getOrDefault(var, 0) + instance.literalCounts.getOrDefault(-var, 0);
            instance.sortedVarCounts.put(var, varScore);
        }

        return this.dpllInternal(instance, model);
    }
}