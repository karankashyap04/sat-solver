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
        // System.out.println("pure symbols propagating: " + pureSymbols);
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
                }
            }
        }
        for (Integer pureSymbol : pureSymbols) {
            // if (pureSymbol == -364) {
            //     System.out.println("-364 literal count: " + instance.literalCounts.get(-364));
            //     System.out.println("364 literal count: " + instance.literalCounts.get(364));
            // }
            // if (model.model.contains(pureSymbol))
            //     System.out.println("ERROR: pure symbol " + pureSymbol + " already in model!");
            // if (model.model.contains(-pureSymbol))
            //     System.out.println("ERROR: pure symbol negation " + (-pureSymbol) + " already in model!");
            // if (instance.literalCounts.containsKey(pureSymbol))
            //     System.out.println("removed pure literal " + pureSymbol + " has non-zero literal count: " + instance.literalCounts.get(pureSymbol));
            // if (instance.literalCounts.containsKey(-pureSymbol))
            //     System.out.println("supposedly non-existent negated pure literal " + (-pureSymbol) + " has non-zero literal count: " + instance.literalCounts.get(-pureSymbol));
        }
        model.model.addAll(pureSymbols); // TODO: create a setModel method
        instance.clauses = updatedClauses; // TODO: maybe create a setClauses method
        for (Integer pureSymbol : pureSymbols) {
            instance.numVars --;
            instance.vars.remove((pureSymbol < 0) ? -1 * pureSymbol : pureSymbol);
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
        // System.out.println("unit clause propagating: " + literal);
        List<Set<Integer>> updatedClauses = new ArrayList<>();
        for (Set<Integer> clause : instance.clauses) {
            if (clause.contains(literal)) {
                // if (literal == -154) {
                //     System.out.println("clause: " + clause);
                // }
                for (Integer clauseLiteral : clause) {
                    instance.reduceLiteralCount(clauseLiteral);
                    if (clauseLiteral.equals(literal) || clauseLiteral.equals(-literal))
                        continue;
                    // if (literal == -154) {
                    //     System.out.println("clause: " + clause);
                    //     System.out.println("clause literal: " + clauseLiteral);
                    //     System.out.println("literal counts of clause literal: " + instance.literalCounts.get(clauseLiteral));
                    //     System.out.println("literal counts of -clause literal: " + instance.literalCounts.get(-clauseLiteral));
                    // }
                    if (!instance.literalCounts.containsKey(clauseLiteral) && instance.literalCounts.containsKey(-clauseLiteral)) {
                        // if (literal == -154) {
                        //     System.out.println("clause: " + clause);
                        //     System.out.println("clause literal: " + clauseLiteral);
                        //     System.out.println("literal counts of clause literal: " + instance.literalCounts.get(clauseLiteral));
                        //     System.out.println("literal counts of -clause literal: " + instance.literalCounts.get(-clauseLiteral));
                        // }
                        if (instance.unitClauses.contains(-clauseLiteral) || instance.unitClauses.contains(clauseLiteral))
                            continue;
                        instance.pureSymbols.add(-clauseLiteral);
                    }
                }
            }
            else if (clause.contains(-literal)) {
                // if (literal == -364) {
                //     System.out.println("clause: " + clause);
                //     System.out.println("unit literal being propagated: " + literal);
                // }
                clause.remove(-literal);
                instance.reduceLiteralCount(-literal);
                if (clause.isEmpty()) {
                    throw new EmptyClauseFoundException("empty clause found!");
                }
                updatedClauses.add(clause);
                if (clause.size() == 1) {
                    for (Integer unitLiteral : clause) {
                        if (!instance.pureSymbols.contains(unitLiteral))
                            instance.unitClauses.add(unitLiteral);
                        break;
                    }
                }
            } else {
                updatedClauses.add(clause);
            }
        }
        // if (model.model.contains(literal))
        //     System.out.println("ERROR: unit clause " + literal + " already in model!");
        // if (model.model.contains(-literal))
        //     System.out.println("ERROR: unit clause negation " + (-literal) + " already in model!");
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
                // if (instance.unitClauses.contains(-364)) {
                //     System.out.println("all unit clauses: " + instance.unitClauses);
                // }
                Integer unitLiteral = 0;
                for (Integer literal : instance.unitClauses) {
                    unitLiteral = literal;
                    break;
                }
                instance.unitClauses.remove(unitLiteral);
                propagateUnitClause(instance, unitLiteral, model);
                return dpllInternal(instance, model);
            }
        } catch (EmptyClauseFoundException e) { // UNSAT
            return new DPLLResult(instance, model, false);
        }

        try {
            Integer branchVariable = this.branchingStrategy.pickBranchingVariable(instance);
            // if (branchVariable == 364)
            //     System.out.println("branching on 364");
            // if (branchVariable == -364)
            //     System.out.println("branching on -364");
            // if (model.model.contains(branchVariable))
            //     System.out.println("branch Variable " + branchVariable + " already in model");
            // if (model.model.contains(-branchVariable))
            //     System.out.println("negated branch variable " + (-branchVariable) + " already in model");

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

        propagatePureSymbols(instance.pureSymbols, instance, model);
        instance.pureSymbols.clear();

        findUnitClauses(instance);

        return this.dpllInternal(instance, model);
    }
}