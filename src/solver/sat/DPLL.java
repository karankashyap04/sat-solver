package solver.sat;

import solver.sat.BranchingStrategies.BranchingStrategy;

import java.util.*;

public class DPLL {

    BranchingStrategy branchingStrategy;
    Set<Integer> remainingClauses = new HashSet<>();
    Map<Integer, Set<Integer>> removedLiterals = new HashMap<>(); // clause idx -> { literals removed from that clause }
    List<Set<Integer>> removedClauseStack = new ArrayList<>();
    List<Map<Integer, Set<Integer>>> removedLiteralStack = new ArrayList<>();
    List<Set<Integer>> assignmentStack = new ArrayList<>();

    public DPLL(BranchingStrategy branchingStrategy) {
        this.branchingStrategy = branchingStrategy;
        this.branchingStrategy.setContext(remainingClauses, removedLiterals);
    }

    private void propagatePureSymbols(Set<Integer> pureSymbols, SATInstance instance, Model model) {
        // filter out any bogus pure symbols
        Set<Integer> filteredPureSymbols = new HashSet<>();
        for (Integer pureSymbol : pureSymbols) {
            if (instance.literalCounts.getOrDefault(pureSymbol, 0) > 0)
                filteredPureSymbols.add(pureSymbol);
        }
        pureSymbols = filteredPureSymbols;

        Set<Integer> newPureSymbols = new HashSet<>();
        Set<Integer> clausesToRemove = new HashSet<>();
        for (Integer clauseIdx : remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            boolean keepClause = true;
            for (Integer pureSymbol : pureSymbols) {
                if (clause.contains(pureSymbol)) { // TODO: maybe check that the pure symbol isn't one of the removed literals (NOTE: this should never happen since you can't infer on the same literal twice)
                    keepClause = false;
                    break;
                }
            }
            if (!keepClause) {
                Set<Integer> clauseRemovedLiterals = removedLiterals.getOrDefault(clauseIdx, new HashSet<>());
                for (Integer literal : clause) {
                    if (clauseRemovedLiterals.contains(literal))
                        continue;
                    instance.reduceLiteralCount(literal);
                    // check if a new pure symbol has been created in doing this
                    if (instance.literalCounts.containsKey(-literal) && !instance.literalCounts.containsKey(literal))
                        newPureSymbols.add(-literal);
                }
                clausesToRemove.add(clauseIdx);
                removedClauseStack.get(removedClauseStack.size() - 1).add(clauseIdx);
            }
        }
        model.model.addAll(pureSymbols);
        assignmentStack.get(assignmentStack.size() - 1).addAll(pureSymbols);

        instance.pureSymbols = newPureSymbols;
        remainingClauses.removeAll(clausesToRemove);
    }

    private void propagateUnitClause(SATInstance instance, Integer literal, Model model) throws EmptyClauseFoundException {
        Set<Integer> clausesToRemove = new HashSet<>();
        boolean emptyClauseFound = false;
        for (Integer clauseIdx : remainingClauses) {
            Set<Integer> clause = instance.clauses.get(clauseIdx);
            if (clause.contains(literal)) { // we need to remove this clause
                Set<Integer> clauseRemovedLiterals = removedLiterals.getOrDefault(clauseIdx, new HashSet<>());
                for (Integer clauseLiteral : clause) {
                    if (clauseRemovedLiterals.contains(clauseLiteral))
                        continue;
                    instance.reduceLiteralCount(clauseLiteral);
                    if (!instance.literalCounts.containsKey(clauseLiteral) && instance.literalCounts.containsKey(-clauseLiteral)
                            && (literal != clauseLiteral)) {
                        instance.pureSymbols.add(-clauseLiteral);
                    }
                }
                clausesToRemove.add(clauseIdx);
                removedClauseStack.get(removedClauseStack.size() - 1).add(clauseIdx);
            } else if (clause.contains(-literal)) { // TODO: maybe check that -literal isn't in removed literals (i don't think that can ever happen since you can't propagate the same literal twice without backtracking)
                instance.reduceLiteralCount(-literal);
                // update removedLiterals
                if (!removedLiterals.containsKey(clauseIdx))
                    removedLiterals.put(clauseIdx, new HashSet<>());
                removedLiterals.get(clauseIdx).add(-literal);
                // update removedLiterals in its stack
                if (!removedLiteralStack.get(removedLiteralStack.size() - 1).containsKey(clauseIdx))
                    removedLiteralStack.get(removedLiteralStack.size() - 1).put(clauseIdx, new HashSet<>());
                removedLiteralStack.get(removedLiteralStack.size() - 1).get(clauseIdx).add(-literal);

                if (clause.size() == removedLiterals.get(clauseIdx).size()) { // check if empty clause
                    emptyClauseFound = true;
                    break;
                }
                if (clause.size() - removedLiterals.get(clauseIdx).size() == 1) { // if new unit clause created
                    for (Integer unitLiteral : clause) {
                        if (removedLiterals.get(clauseIdx).contains(unitLiteral))
                            continue;
                        instance.unitClauses.add(unitLiteral);
                        break;
                    }
                }
            }
        }
        remainingClauses.removeAll(clausesToRemove);
        model.model.add(literal);
        assignmentStack.get(assignmentStack.size() - 1).add(literal);
        if (emptyClauseFound) {
            throw new EmptyClauseFoundException("empty clause found!");
        }
    }

    private boolean isSAT(SATInstance instance) {
        // checks if guaranteed to be sat already
        return remainingClauses.size() == 0;
    }

    private void backtrack(SATInstance instance, Model model) {
        Set<Integer> stackRemovedClauses = removedClauseStack.remove(removedClauseStack.size() - 1);
        Map<Integer, Set<Integer>> stackRemovedLiterals = removedLiteralStack.remove(removedLiteralStack.size() - 1);
        Set<Integer> stackAssignments = assignmentStack.remove(assignmentStack.size() - 1);

        // undo the removed clauses
        for (Integer clauseIdx : stackRemovedClauses) {
            if (remainingClauses.contains(clauseIdx)) {
                System.out.println("UNEXPECTED: remainingClauses contains clauses that are removed according to the stack!");
            }
            remainingClauses.add(clauseIdx);

            Set<Integer> clauseRemovedLiterals = removedLiterals.getOrDefault(clauseIdx, new HashSet<>());
            for (Integer literal : instance.clauses.get(clauseIdx)) {
                if (clauseRemovedLiterals.contains(literal))
                    continue;
                instance.increaseLiteralCount(literal);
            }
        }

        // undo the removed literals
        for (Integer clauseIdx : stackRemovedLiterals.keySet()) {
            Set<Integer> clauseRemovedLiterals = removedLiterals.get(clauseIdx);
            for (Integer removedLiteral : stackRemovedLiterals.get(clauseIdx)) {
                if (!clauseRemovedLiterals.contains(removedLiteral))
                    System.out.println("UNEXPECTED: removedLiterals doesn't contain literal that was removed according to the stack!");
                clauseRemovedLiterals.remove(removedLiteral);
                instance.increaseLiteralCount(removedLiteral);
            }
        }

        // undo the assignments
        if (!model.model.containsAll(stackAssignments))
            System.out.println("UNEXPECTED: model doesn't contain all assignments stored in the stack!");
        model.model.removeAll(stackAssignments);

        instance.unitClauses.clear();
        instance.pureSymbols.clear();
    }

    private DPLLResult dpllInternal(SATInstance instance, Model model) {
        if (isSAT(instance)) {
            return new DPLLResult(instance, model, true);
        }

        if (!instance.pureSymbols.isEmpty()) {
            propagatePureSymbols(instance.pureSymbols, instance, model);
            return dpllInternal(instance, model);
        }

        try {
            if (!instance.unitClauses.isEmpty()) {
                Integer unitLiteral = 0;
                for (Integer unitClause : instance.unitClauses) {
                    unitLiteral = unitClause;
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

            // POSITIVE ASSUMPTION
            // 1. make new entries on the stack
            removedClauseStack.add(new HashSet<>());
            removedLiteralStack.add(new HashMap<>());
            assignmentStack.add(new HashSet<>());

            // 2. mark the branching variable as a unit clause that needs to be propagated
            instance.unitClauses.add(branchVariable);

            // 3. recurse with positive assumption
            DPLLResult positiveAssumptionResult = dpllInternal(instance, model);
            if (positiveAssumptionResult.isSAT) {
                return positiveAssumptionResult;
            }

            // BACKTRACKING -- undo effects of positive assumption
            backtrack(instance, model);

            // NEGATIVE ASSUMPTION
            // 1. mark the negated branching variable as a unit clause that needs to be propagated
            instance.unitClauses.add(-branchVariable);

            // 2. recurse with negative assumption
            return dpllInternal(instance, model);
        }
        catch (NoVariableFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private boolean initiallyHasEmptyClause(SATInstance instance) {
        for (Set<Integer> clause : instance.clauses) {
            if (clause.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void findInitialUnitClauses(SATInstance instance) {
        for (Set<Integer> clause : instance.clauses) {
            if (clause.size() == 1) {
                for (Integer literal : clause) {
                    instance.unitClauses.add(literal);
                    break;
                }
            }
        }
    }

    public DPLLResult dpll(SATInstance instance, Model model) {
        if (initiallyHasEmptyClause(instance)) {
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

        findInitialUnitClauses(instance);

        // populate remaining clauses
        for (int i = 0; i < instance.clauses.size(); i++)
            remainingClauses.add(i);
        assert remainingClauses.size() == instance.clauses.size();

        // initialize stacks with empty elements (these initial elements should always remain on the stack -- never used
        // while backtracking etc since these are from before we ever branch)
        removedClauseStack.add(new HashSet<>());
        removedLiteralStack.add(new HashMap<>());
        assignmentStack.add(new HashSet<>());

        return this.dpllInternal(instance, model);
    }
}