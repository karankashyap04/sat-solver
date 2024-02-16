package solver.sat;

import solver.sat.BranchingStrategies.BranchingStrategy;

import java.util.*;

public class DPLL {

    BranchingStrategy branchingStrategy;
    Set<Integer> remainingClauses = new HashSet<>(); // indices of removed clauses
    Map<Integer, Set<Integer>> removedLiterals = new HashMap<>(); // clause idx -> literals removed from the clause
    List<Set<Integer>> removedClauseStack = new ArrayList<>();
    List<Map<Integer, Set<Integer>>> removedLiteralsStack = new ArrayList<>();
    List<Set<Integer>> assignmentStack = new ArrayList<>();

    public DPLL(BranchingStrategy branchingStrategy) {
        this.branchingStrategy = branchingStrategy;
    }

    private void propagatePureSymbols(Set<Integer> pureSymbols, SATInstance instance, Model model) {
//        List<Set<Integer>> updatedClauses = new ArrayList<>();
//        for (Set<Integer> clause : instance.clauses) {
        Set<Integer> newPureSymbols = new HashSet<>();
        Set<Integer> clausesToRemove = new HashSet<>();
        for (Integer i : remainingClauses) {
            Set<Integer> clause = instance.clauses.get(i);
            boolean keepClause = true;
            for (Integer pureSymbol : pureSymbols) {
                if (clause.contains(pureSymbol)) {
//                    instance.numClauses--;
                    keepClause = false;
                    break;
                }
            }
            if (keepClause) {
//                updatedClauses.add(clause);
                continue;
            } else {
                for (Integer literal : clause) {
                    instance.updateVarCount(literal, -1);
                    instance.reduceLiteralCount(literal);
                    if (!instance.literalCounts.containsKey(literal) && instance.literalCounts.containsKey(-literal)) {
//                        instance.pureSymbols.add(-literal);
                        newPureSymbols.add(-literal);
                    }
                }
                if (removedClauseStack.isEmpty()) {
                    removedClauseStack.add(new HashSet<>());
                }
                // clause i has been removed
                removedClauseStack.get(removedClauseStack.size() - 1).add(i);
//                remainingClauses.remove(i);
                clausesToRemove.add(i);
            }
        }
        model.model.addAll(pureSymbols); // TODO: create a setModel method
        if (assignmentStack.isEmpty()) {
            assignmentStack.add(new HashSet<>());
        }
        assignmentStack.get(assignmentStack.size() - 1).addAll(pureSymbols);
        instance.pureSymbols = newPureSymbols;

        for (Integer clauseIdx : clausesToRemove) {
            remainingClauses.remove(clauseIdx);
        }

    }


    private void findUnitClauses(SATInstance instance) {
        for (Integer clauseIdx : remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            if (clause.size() == 1) {
                for (Integer literal : clause) {
                    instance.unitClauses.add(literal);
                    break;
                }
            }
        }
    }

    private void propagateUnitClause(SATInstance instance, Integer literal, Model model) throws EmptyClauseFoundException {
//        List<Set<Integer>> updatedClauses = new ArrayList<>();
//        for (Set<Integer> clause : instance.clauses) {
        Set<Integer> clausesToRemove = new HashSet<>();
        for (Integer clauseIdx : remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            if (clause.contains(-literal)) {
//                clause.remove(-literal);
                if (!removedLiterals.containsKey(clauseIdx)) {
                    removedLiterals.put(clauseIdx, new HashSet<>());
                }
                removedLiterals.get(clauseIdx).add(-literal);
                if (removedLiteralsStack.isEmpty()) {
                    removedLiteralsStack.add(new HashMap<>());
                }
                if (!removedLiteralsStack.get(removedLiteralsStack.size() - 1).containsKey(clauseIdx)) {
                    removedLiteralsStack.get(removedLiteralsStack.size() - 1).put(clauseIdx, new HashSet<>());
                }
                removedLiteralsStack.get(removedLiteralsStack.size() - 1).get(clauseIdx).add(-literal);
                instance.updateVarCount(literal, -1);
                instance.reduceLiteralCount(-literal);
                if (clause.size() == removedLiterals.get(clauseIdx).size()) {
                    throw new EmptyClauseFoundException("empty clause found!");
                }
//                updatedClauses.add(clause);
                if (clause.size() - removedLiterals.get(clauseIdx).size() == 1) {
                    for (Integer unitLiteral : clause) {
                        instance.unitClauses.add(unitLiteral);
                        break;
                    }
                }
            }
            else if (!clause.contains(literal)) {
                continue;
            } else { // this clause is being removed since we're unit propagating a literal that is in this clause
                for (Integer clauseLiteral : clause) {
                    if (removedLiterals.getOrDefault(clauseIdx, new HashSet<>()).contains(clauseLiteral))
                        continue;
                    instance.updateVarCount(clauseLiteral, -1);
                    instance.reduceLiteralCount(clauseLiteral);
                    if (!instance.literalCounts.containsKey(clauseLiteral) && instance.literalCounts.containsKey(-clauseLiteral)) {
                        instance.pureSymbols.add(-clauseLiteral);
                    }
                }
                if (removedClauseStack.isEmpty()) {
                    removedClauseStack.add(new HashSet<>());
                }
                removedClauseStack.get(removedClauseStack.size() - 1).add(clauseIdx);
                clausesToRemove.add(clauseIdx);
            }
        }
        model.model.add(literal);

        if (assignmentStack.isEmpty())
            assignmentStack.add(new HashSet<>());
        assignmentStack.get(assignmentStack.size() - 1).add(literal);

        for (Integer clauseIdx : clausesToRemove) {
            remainingClauses.remove(clauseIdx);
        }

    }

    private boolean isSAT(SATInstance instance) {
        // checks if guaranteed to be sat already
        return remainingClauses.size() == 0;
    }

    private  DPLLResult dpllInternal(SATInstance instance, Model model) {
        System.out.println(instance.sortedVarCounts);
        if (isSAT(instance)) {
            return new DPLLResult(instance, model, true);
        }

        if (!instance.pureSymbols.isEmpty()) {
            propagatePureSymbols(instance.pureSymbols, instance, model);
//            instance.pureSymbols.clear();
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

        try {
            Integer branchVariable = this.branchingStrategy.pickBranchingVariable(instance);

            // positive assumption
            // new elements on stack
            removedClauseStack.add(new HashSet<>());
            removedLiteralsStack.add(new HashMap<>());
            assignmentStack.add(new HashSet<>());

            model.model.add(branchVariable);
            assignmentStack.get(assignmentStack.size() - 1).add(branchVariable);
            try {
                propagateUnitClause(instance, branchVariable, model);
                DPLLResult positiveResult = dpllInternal(instance, model);

                // check if satisfied
                if (positiveResult.isSAT)
                    return positiveResult;
            } catch (EmptyClauseFoundException e) {
                // do nothing here (positive assumption led to UNSAT)
            }

            // backtrack
            Set<Integer> positiveRemovedClauses = removedClauseStack.remove(removedClauseStack.size() - 1);
            Map<Integer, Set<Integer>> positiveRemovedLiterals = removedLiteralsStack.remove(removedClauseStack.size() - 1);
            Set<Integer> positiveAssignment = assignmentStack.remove(assignmentStack.size() - 1);

            // as we undo changes, we need to update the models, the global vars, and literal counts, sortedVarCounts.
            for (Integer clauseIdx : positiveRemovedClauses) {
                remainingClauses.add(clauseIdx);
                for (Integer literal : instance.clauses.get(clauseIdx)) {
                    if (!removedLiterals.getOrDefault(clauseIdx, new HashSet<>()).contains(literal)) {
                        instance.updateVarCount(literal, 1);
                        instance.increaseLiteralCount(literal);
                    }
                }
            }

            for (Integer clauseIdx : positiveRemovedLiterals.keySet()) {
                for (Integer removedLiteral : positiveRemovedLiterals.get(clauseIdx)) {
                    removedLiterals.get(clauseIdx).remove(removedLiteral);

                    instance.updateVarCount(removedLiteral, 1);
                    instance.increaseLiteralCount(removedLiteral);
                }
            }

            for (Integer literal : positiveAssignment) {
                model.model.remove(literal);
            }

            // TODO: clear out unit clauses and pure symbols vars in instance
            instance.unitClauses.clear();
            instance.pureSymbols.clear();

            // negative assumption
            // new elements on set
            removedClauseStack.add(new HashSet<>());
            removedLiteralsStack.add(new HashMap<>());
            assignmentStack.add(new HashSet<>());

            model.model.add(-branchVariable);
            assignmentStack.get(assignmentStack.size() - 1).add(-branchVariable);
            try {
                propagateUnitClause(instance, branchVariable, model);
                DPLLResult negativeResult = dpllInternal(instance, model);
                return negativeResult;
            } catch (EmptyClauseFoundException e) {
                // do nothing here (positive assumption led to UNSAT)
                return new DPLLResult(instance, model, false);
            }

        } catch (NoVariableFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // only used at beginning
    private boolean initiallyHasEmptyClause(SATInstance instance) {
        for (Set<Integer> clause : instance.clauses) {
            if (clause.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public DPLLResult dpll(SATInstance instance, Model model) {
        if (initiallyHasEmptyClause(instance)) {
            return new DPLLResult(instance, model, false);
        }

        // populate remainingClauses
        for (int i = 0; i < instance.clauses.size(); i++) {
            remainingClauses.add(i);
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
            Set<Integer> varScoreVars = instance.sortedVarCounts.get(varScore);
            if (varScoreVars == null) {
                varScoreVars = new HashSet<>();
                varScoreVars.add(var);
                instance.sortedVarCounts.put(varScore, varScoreVars);
            } else
                varScoreVars.add(var);
        }

        return this.dpllInternal(instance, model);
    }
}