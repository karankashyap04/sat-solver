#include <iostream>
#include <cassert>
#include "include/DPLL.hpp"
#include "include/utils.hpp"

/**
 * Class that runs the DPLL algorithm on a given SATInstance
*/
DPLL::DPLL(BranchingStrategy* branchingStrategy, SATInstance* instance, Model* model) {
    this->branchingStrategy = branchingStrategy;
    this->instance = instance;
    this->model = model;

    this->remainingClauses = new std::unordered_set<int>();
    this->removedLiterals = new std::unordered_map<int, std::unordered_set<int>*>();
    this->removedClauseStack = new std::vector<std::unordered_set<int>*>();
    this->removedLiteralStack = new std::vector<std::unordered_map<int, std::unordered_set<int>*>*>();
    this->assignmentStack = new std::vector<std::unordered_set<int>*>();

    branchingStrategy->setContext(this->remainingClauses, this->removedLiterals);
}

/**
 * Returns the variable for this literal
*/
int getVar(int literal) {
    return literal < 0 ? -literal : literal;
}

/**
 * Propagates all pure symbols found so far
*/
void DPLL::propagatePureSymbols() {
    std::unordered_set<int> *filteredPureSymbols = new std::unordered_set<int>();
    // exclude any pure symbols whose count is 0 (should be none like this -- extra
    // safety check)
    for (int pureSymbol : *(this->instance->pureSymbols)) {
        if (instance->getLiteralCounts(pureSymbol)->at(getVar(pureSymbol) - 1) > 0)
            filteredPureSymbols->insert(pureSymbol);
    }
    delete(instance->pureSymbols); // delete old memory on the heap
    instance->pureSymbols = filteredPureSymbols;

    std::unordered_set<int> *newPureSymbols = new std::unordered_set<int>(); // newly created pure symbols go here
    std::vector<int> clausesToRemove; // clauses that need to be removed
    for (int clauseIdx : *this->remainingClauses) { // only loop through remaining clauses
        std::unordered_set<int> *clause = this->instance->clauses->at(clauseIdx);
        bool keepClause = true;
        for (int pureSymbol : *instance->pureSymbols) {
            if (setContains(clause, pureSymbol)) { // if clause contains a pure symbol, remove the clause
                keepClause = false;
                break;
            }
        }
        if (!keepClause) { // this clause needs to be removed
            std::unordered_set<int> emptySet;
            std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &emptySet);
            for (int literal : *clause) {
                // reduce the counts of all literals in the clause (other than literals that are already removed)
                if (setContains(clauseRemovedLiterals, literal))
                    continue;
                this->instance->reduceLiteralCount(literal);
                int literalVar = getVar(literal);
                // check if a new pure symbol has been created as a result of removing this literal
                if (instance->getLiteralCounts(-literal)->at(literalVar - 1) > 0 && instance->getLiteralCounts(literal)->at(literalVar - 1) == 0) {
                    newPureSymbols->insert(-literal);
                }
            }
            clausesToRemove.push_back(clauseIdx);
            this->removedClauseStack->at(this->removedClauseStack->size() - 1)->insert(clauseIdx);
        }
    }

    // update assignments in model
    for (int pureSymbol : *this->instance->pureSymbols) {
        this->model->model->insert(pureSymbol);
        this->assignmentStack->at(this->assignmentStack->size() - 1)->insert(pureSymbol);
    }
    
    delete(this->instance->pureSymbols); // delete filteredPureSymbols
    this->instance->pureSymbols = newPureSymbols;
    for (int clauseIdx : clausesToRemove) {
        this->remainingClauses->erase(clauseIdx); // update remaining clauses
    }
}

/**
 * Propagates the given literal as a unit clause
*/
void DPLL::propagateUnitClause(int literal) {
    std::unordered_set<int> empty_set;
    std::unordered_set<int> clausesToRemove; // used to update remaining clauses after unit propagation
    bool emptyClauseFound = false;
    
    for (int clauseIdx : *(this->remainingClauses)) {
        std::unordered_set<int> *clause = this->instance->clauses->at(clauseIdx);
        // remove clauses that contain the literal being propagated
        if (setContains(clause, literal)) {
            // get literals removed from this clause
            std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &empty_set);
            for (int clauseLiteral : *clause) {
                // if the clauseLiteral has been removed from the clause, do nothing
                if (setContains(clauseRemovedLiterals, clauseLiteral)) {
                    continue;
                }
                
                // decrease literal count for clauseLiteral because its clause is being removed
                this->instance->reduceLiteralCount(clauseLiteral);
                // go to next iteration if clauseLiteral or its negation are already tracked as unit clauses
                if (setContains(this->instance->unitClauses, -clauseLiteral) || setContains(this->instance->unitClauses, clauseLiteral)) {
                    continue;
                }
                
                int clauseLiteralVar = getVar(clauseLiteral);
                // check if new pure symbol is created
                if (this->instance->getLiteralCounts(clauseLiteral)->at(clauseLiteralVar - 1) == 0 && this->instance->getLiteralCounts(-clauseLiteral)->at(clauseLiteralVar - 1) > 0
                    && (literal != clauseLiteral) && (literal != -clauseLiteral)) {
                    this->instance->pureSymbols->insert(-clauseLiteral);
                }
            }
            
            // record (locally and globally) clauses removed by unit propagation
            clausesToRemove.insert(clauseIdx);
            this->removedClauseStack->at(this->removedClauseStack->size() - 1)->insert(clauseIdx);
        } else if (setContains(clause, -literal)) {
            this->instance->reduceLiteralCount(-literal);
            // update removed literals
            if (!mapContainsKey(this->removedLiterals, clauseIdx)) {
                (*this->removedLiterals)[clauseIdx] = new std::unordered_set<int>();
            }
            this->removedLiterals->at(clauseIdx)->insert(-literal);
            // update removedLiterals in its stack
            if (!mapContainsKey(this->removedLiteralStack->at(removedLiteralStack->size() -  1), clauseIdx)) {
                (*this->removedLiteralStack->at(removedLiteralStack->size() - 1))[clauseIdx] = new std::unordered_set<int>();
            }
            this->removedLiteralStack->at(removedLiteralStack->size() - 1)->at(clauseIdx)->insert(-literal);

            // check if empty clause
            if (clause->size() == this->removedLiterals->at(clauseIdx)->size()) {
                emptyClauseFound = true;
                break;
            }

            // if new unit clause created
            if (clause->size() - this->removedLiterals->at(clauseIdx)->size() == 1) {
                for (int unitLiteral : *clause) {
                    if (setContains(removedLiterals->at(clauseIdx), unitLiteral)) 
                        continue;
                    if (!setContains(this->instance->pureSymbols, unitLiteral)) 
                        this->instance->unitClauses->insert(unitLiteral);
                    break;
                }
            }
        }
    }

    // update remaining clauses
    for (int clauseIdx : clausesToRemove) {
        this->remainingClauses->erase(clauseIdx);
    }
    // update assignments
    this->model->model->insert(literal);
    this->assignmentStack->at(assignmentStack->size() - 1)->insert(literal);
    if (emptyClauseFound) {
        throw std::runtime_error("Empty clause found!");
    }

}

/**
 * Checks if a SAT configuration has been reached
*/
bool DPLL::isSAT() {
    // sat if there are no clauses left to be satisfied
    return this->remainingClauses->size() == 0;
}

/**
 * Checks if the instance (initially) contains an empty clause (trivially UNSAT)
*/
bool DPLL::initiallyHasEmptyClause() {
    for (std::unordered_set<int>* clause : *(this->instance->clauses)) {
        if (clause->empty()) {
            return true;
        }
    }
    return false;
}

/**
 * Finds the unit clauses initially present in the instance
*/
void DPLL::findInitialUnitClauses() {
    std::unordered_set<int> emptySet;
    for (int clauseIdx : *(this->remainingClauses)) {
        std::unordered_set<int>* clause = this->instance->clauses->at(clauseIdx);
        std::unordered_set<int>* clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &emptySet);
        
        if (clause->size() - clauseRemovedLiterals->size() == 1) { // if the number of literals left in the clause is 1, it is a unit clause
            for (int literal : *clause) {
                if (setContains(clauseRemovedLiterals, literal)) // skip if this literal has been removed
                    continue;
                if (!setContains(this->instance->pureSymbols, literal)) // if already in pure symbols, don't add again (will be propagated anyways)
                    this->instance->unitClauses->insert(literal);
                break;
            }
        }
    }
}

/**
 * Performs backtracking
*/
void DPLL::backtrack() {
    // pops off the last elements from the stacks
    std::unordered_set<int> *stackRemovedClauses = this->removedClauseStack->back();
    this->removedClauseStack->pop_back();
    std::unordered_map<int, std::unordered_set<int>*> *stackRemovedLiterals = this->removedLiteralStack->back();
    this->removedLiteralStack->pop_back();
    std::unordered_set<int> *stackAssignments = this->assignmentStack->back();
    this->assignmentStack->pop_back();

    // undo the removed clauses (undo clauses removed from instnce since last branch)
    for (int clauseIdx : *stackRemovedClauses) { // loop through all clauses removed since the branch being backtracked
        if (setContains(this->remainingClauses, clauseIdx)) {
            std::cout << "UNEXPECTED: remainingClauses contains clauses that are removed according to the stack!" << std::endl;
        }
        this->remainingClauses->insert(clauseIdx); // the clause is no longer removed

        std::unordered_set<int> emptySet;
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &emptySet);
        for (int literal : *(this->instance->clauses->at(clauseIdx))) {
            // ignore literals if they are in removedLiterals.
            // if something is in removedLiterals, these are literals that were removed from the clause before
            // the clause itself was removed (when removing a clause as a whole, we don't update removedLiterals for the clause).
            // we want to make sure not to unroll the removal of literals from the clause that took place
            // before the branch being undone.
            if (setContains(clauseRemovedLiterals, literal))
                continue;
            this->instance->increaseLiteralCount(literal);
        }
    }

    // undo the removed literals (undo literals removed from clauses since the last branch)
    for (const auto& pair : *stackRemovedLiterals) {
        int clauseIdx = pair.first;
        std::unordered_set<int> *clauseRemovedLiterals = this->removedLiterals->at(clauseIdx);
        for (int removedLiteral : *pair.second) {
            if (!setContains(clauseRemovedLiterals, removedLiteral))
                std::cout << "UNEXPECTED: removedLiterals doesn't contain literal that was removed according to the stack!" << std::endl;
            clauseRemovedLiterals->erase(removedLiteral); // this literal is no longer removed from this clause
            this->instance->increaseLiteralCount(removedLiteral); // update the count for the literal 
        }
    }

    // undo the assignments made since thhe last branch
    for (int assignment : *stackAssignments) {
        if (!setContains(this->model->model, assignment))
            std::cout << "UNEXPECTED: model doesn't contain all assignments stored in the stack!" << std::endl;
    }
    for (int assignment : *stackAssignments) {
        this->model->model->erase(assignment);
    }
    
    // now that we have backtracked, no unit clauses or pure symbols found here are valid anymore
    this->instance->unitClauses->clear();
    this->instance->pureSymbols->clear();

    // clean up memory created on the heap:
    delete(stackRemovedClauses);
    for (const auto& pair : *stackRemovedLiterals) {
        delete(pair.second);
    }
    delete(stackRemovedLiterals);
    delete(stackAssignments);
}


/**
 * Iteratively runs DPLL
*/
DPLLResult* DPLL::dpllInternal() {
    // initialize stack of branching literals
    std::vector<int> dpllLiteralStack;
    // indicates whether the negation of the literal at same index in
    // dpllLiteralStack has been used in branching
    std::vector<bool> dpllNegCheckedStack;
    // stores whether the current branch has evaluated to unsat
    bool unsatReached = false;

    while (!isSAT()) {
        if (unsatReached) {
            // return UNSAT if all branches checked
            if (dpllLiteralStack.empty())
                return new DPLLResult(this->instance, this->model, false);
            
            // if negative branch checked, remove literal from stack and backtrack again
            bool negativeBranchChecked = dpllNegCheckedStack.back();
            if (negativeBranchChecked) {
                dpllLiteralStack.pop_back();
                dpllNegCheckedStack.pop_back();
                continue;
            }
            // if negative branch not yet checked, backtrack and run DPLL on
            // negation of branching variable 
            backtrack();
            dpllLiteralStack.back() *= -1;
            instance->unitClauses->insert(dpllLiteralStack.back());
            dpllNegCheckedStack.back() = true;
            unsatReached = false;
            continue;
        }

        // propagate pure symbols
        if (!this->instance->pureSymbols->empty()) {
            propagatePureSymbols();
            continue;
        }

        try {
            // propagate a single unit clause
            if (!this->instance->unitClauses->empty()) {
                int unitLiteral = 0;
                for (int unitClause : *this->instance->unitClauses) {
                    unitLiteral = unitClause;
                    break;
                }

                this->instance->unitClauses->erase(unitLiteral);
                propagateUnitClause(unitLiteral);
                continue;
            }
        } catch (...) {
            unsatReached = true;
            continue;
        }

        // we need to branch!
        try {
            int branchVariable = this->branchingStrategy->pickBranchingVariable(this->instance);
            dpllLiteralStack.push_back(branchVariable);
            dpllNegCheckedStack.push_back(false);

            // make new entries on the global stacks
            this->removedClauseStack->push_back(new std::unordered_set<int>());
            this->removedLiteralStack->push_back(new std::unordered_map<int, std::unordered_set<int>*>());
            this->assignmentStack->push_back(new std::unordered_set<int>());

            // mark the branching variable as a unit clause that needs to be propagated
            this->instance->unitClauses->insert(branchVariable);
            continue;
        } catch (const std::exception& e) {
            std::cerr << "Exception caught: " << e.what() << std::endl;
            return NULL;
        }

    }
    
    // we have found a SAT assignment (exited the loop)
    return new DPLLResult(this->instance, this->model, true);
}

/**
 * Sets up state for dpll algorithm (which will be run in dpllInternal, above)
*/
DPLLResult* DPLL::dpll() {
    if (initiallyHasEmptyClause()) { // if trivially UNSAT  (instance initially has empty clause), return UNSAT
        return new DPLLResult(this->instance, this->model, false);
    }
    
    // initialize stacks with empty elements (these initial elements should always remain on the stack -- never used
    // while backtracking etc since these are from before we ever branch)
    this->removedClauseStack->push_back(new std::unordered_set<int>());
    this->removedLiteralStack->push_back(new std::unordered_map<int, std::unordered_set<int>*>());
    this->assignmentStack->push_back(new std::unordered_set<int>());
    
    // populate remainingClauses -- put all clauses there (all clauses are currently unsatisfied)
    for (int i = 0; i < this->instance->clauses->size(); i++) {
        this->remainingClauses->insert(i);
    }
    assert(this->remainingClauses->size() == this->instance->clauses->size()); // safety check -- all clauses should be remaining

    // find initially occurring pure symbols and populate literal counts
    for (std::unordered_set<int> *clause : *this->instance->clauses) {
        for (int literal : *clause) {
            this->instance->increaseLiteralCount(literal);
            if (this->instance->getLiteralCounts(-literal)->at(getVar(-literal) - 1) > 0) {
                // if literal and -literal both exist, make sure neither is marked as a pure symbol
                this->instance->pureSymbols->erase(literal);
                this->instance->pureSymbols->erase(-literal);
            } else { // if -literal doesn't exist, literal is a pure symbol
                this->instance->pureSymbols->insert(literal);
            }
        }
    }

    propagatePureSymbols(); // propagate the pure symbols

    findInitialUnitClauses(); // find the initial unit clauses (will be propagated within dpllInternal)
    
    return dpllInternal(); // runs DPLL
}
