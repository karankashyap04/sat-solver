#include <iostream>
#include <cassert>
#include "include/DPLL.hpp"
#include "include/utils.hpp"

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

int getVar(int literal) {
    return literal < 0 ? -literal : literal;
}

void DPLL::propagatePureSymbols() {
    std::unordered_set<int> *filteredPureSymbols = new std::unordered_set<int>();
    for (int pureSymbol : *(this->instance->pureSymbols)) {
        // if (getOrDefault(instance->literalCounts, pureSymbol, 0) > 0)
        //     filteredPureSymbols->insert(pureSymbol);
        if (instance->getLiteralCounts(pureSymbol)->at(getVar(pureSymbol) - 1) > 0)
            filteredPureSymbols->insert(pureSymbol);
    }
    delete(instance->pureSymbols); // delete old memory on the heap
    instance->pureSymbols = filteredPureSymbols;

    std::unordered_set<int> *newPureSymbols = new std::unordered_set<int>();
    std::vector<int> clausesToRemove;
    for (int clauseIdx : *this->remainingClauses) {
        std::unordered_set<int> *clause = this->instance->clauses->at(clauseIdx);
        bool keepClause = true;
        for (int pureSymbol : *instance->pureSymbols) {
            if (setContains(clause, pureSymbol)) {
                keepClause = false;
                break;
            }
        }
        if (!keepClause) {
            std::unordered_set<int> emptySet;
            std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &emptySet);
            for (int literal : *clause) {
                if (setContains(clauseRemovedLiterals, literal))
                    continue;
                this->instance->reduceLiteralCount(literal);
                // if (mapContainsKey(instance->literalCounts, -literal) && !mapContainsKey(instance->literalCounts, literal)) {
                //     newPureSymbols->insert(-literal);
                // }
                int literalVar = getVar(literal);
                if (instance->getLiteralCounts(-literal)->at(literalVar - 1) > 0 && instance->getLiteralCounts(literal)->at(literalVar - 1) == 0) {
                    newPureSymbols->insert(-literal);
                }
            }
            clausesToRemove.push_back(clauseIdx);
            this->removedClauseStack->at(this->removedClauseStack->size() - 1)->insert(clauseIdx);
        }
    }
    // this->model->model->insert(this->instance->pureSymbols->begin(), this->instance->pureSymbols->end());
    // this->assignmentStack->at(this->assignmentStack->size() - 1)->insert(this->instance->pureSymbols->begin(), this->instance->pureSymbols->end());

    for (int pureSymbol : *this->instance->pureSymbols) {
        this->model->model->insert(pureSymbol);
        this->assignmentStack->at(this->assignmentStack->size() - 1)->insert(pureSymbol);
    }
    
    delete(this->instance->pureSymbols); // this should delete filteredPureSymbols
    this->instance->pureSymbols = newPureSymbols;
    for (int clauseIdx : clausesToRemove) {
        this->remainingClauses->erase(clauseIdx);
    }
}

void DPLL::propagateUnitClause(int literal) {
    std::unordered_set<int> empty_set;
    std::unordered_set<int> clausesToRemove;
    bool emptyClauseFound = false;
    
    for (int clauseIdx : *(this->remainingClauses)) {
        std::unordered_set<int> *clause = this->instance->clauses->at(clauseIdx);
        if (setContains(clause, literal)) {
            std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &empty_set);
            for (int clauseLiteral : *clause) {
                if (setContains(clauseRemovedLiterals, clauseLiteral)) {
                    continue;
                }
                
                this->instance->reduceLiteralCount(clauseLiteral);
                if (setContains(this->instance->unitClauses, -clauseLiteral) || setContains(this->instance->unitClauses, clauseLiteral)) {
                    continue;
                }

                int clauseLiteralVar = getVar(clauseLiteral);
                // if (!mapContainsKey(this->instance->literalCounts, clauseLiteral) && mapContainsKey(this->instance->literalCounts, -clauseLiteral)
                if (this->instance->getLiteralCounts(clauseLiteral)->at(clauseLiteralVar - 1) == 0 && this->instance->getLiteralCounts(-clauseLiteral)->at(clauseLiteralVar - 1) > 0
                    && (literal != clauseLiteral) && (literal != -clauseLiteral)) {
                    this->instance->pureSymbols->insert(-clauseLiteral);
                }
            }
            
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
    for (int clauseIdx : clausesToRemove) {
        this->remainingClauses->erase(clauseIdx);
    }
    this->model->model->insert(literal);
    this->assignmentStack->at(assignmentStack->size() - 1)->insert(literal);
    if (emptyClauseFound) {
        throw std::runtime_error("Empty clause found!");
    }

}

bool DPLL::isSAT() {
    return this->remainingClauses->size() == 0;
}

bool DPLL::initiallyHasEmptyClause() {
    for (std::unordered_set<int>* clause : *(this->instance->clauses)) {
        if (clause->empty()) {
            return true;
        }
    }
    return false;
}

void DPLL::findInitialUnitClauses() {
    std::unordered_set<int> emptySet;
    for (int clauseIdx : *(this->remainingClauses)) {
        std::unordered_set<int>* clause = this->instance->clauses->at(clauseIdx);
        std::unordered_set<int>* clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &emptySet);
        
        if (clause->size() - clauseRemovedLiterals->size() == 1) {
            for (int literal : *clause) {
                if (setContains(clauseRemovedLiterals, literal))
                    continue;
                if (!setContains(this->instance->pureSymbols, literal))
                    this->instance->unitClauses->insert(literal);
                break;
            }
        }
    }
}

void DPLL::backtrack() {
    std::unordered_set<int> *stackRemovedClauses = this->removedClauseStack->back();
    this->removedClauseStack->pop_back();
    std::unordered_map<int, std::unordered_set<int>*> *stackRemovedLiterals = this->removedLiteralStack->back();
    this->removedLiteralStack->pop_back();
    std::unordered_set<int> *stackAssignments = this->assignmentStack->back();
    this->assignmentStack->pop_back();

    // undo the removed clauses
    for (int clauseIdx : *stackRemovedClauses) {
        if (setContains(this->remainingClauses, clauseIdx)) {
            std::cout << "UNEXPECTED: remainingClauses contains clauses that are removed according to the stack!" << std::endl;
        }
        this->remainingClauses->insert(clauseIdx);

        std::unordered_set<int> emptySet;
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->removedLiterals, clauseIdx, &emptySet);
        for (int literal : *(this->instance->clauses->at(clauseIdx))) {
            if (setContains(clauseRemovedLiterals, literal))
                continue;
            this->instance->increaseLiteralCount(literal);
        }
    }

    // undo the removed literals
    for (const auto& pair : *stackRemovedLiterals) {
        int clauseIdx = pair.first;
        std::unordered_set<int> *clauseRemovedLiterals = this->removedLiterals->at(clauseIdx);
        for (int removedLiteral : *pair.second) {
            if (!setContains(clauseRemovedLiterals, removedLiteral))
                std::cout << "UNEXPECTED: removedLiterals doesn't contain literal that was removed according to the stack!" << std::endl;
            clauseRemovedLiterals->erase(removedLiteral);
            this->instance->increaseLiteralCount(removedLiteral);
        }
    }

    // undo the assignments
    for (int assignment : *stackAssignments) {
        if (!setContains(this->model->model, assignment))
            std::cout << "UNEXPECTED: model doesn't contain all assignments stored in the stack!" << std::endl;
    }
    // this->model->model->erase(stackAssignments->begin(), stackAssignments->end());
    for (int assignment : *stackAssignments) {
        this->model->model->erase(assignment);
    }
    
    this->instance->unitClauses->clear();
    this->instance->pureSymbols->clear();

    delete(stackRemovedClauses);
    for (const auto& pair : *stackRemovedLiterals) {
        delete(pair.second);
    }
    delete(stackRemovedLiterals);
    delete(stackAssignments);
}


DPLLResult* DPLL::dpllInternal() {
    if (isSAT()) {
        return new DPLLResult(this->instance, this->model, true);
    }
    // std::cout << "isSAT check done" << std::endl;

    if (!this->instance->pureSymbols->empty()) {
        propagatePureSymbols();
        return dpllInternal();
    }

    try {
        if (!this->instance->unitClauses->empty()) {
            int unitLiteral = 0;
            for (int unitClause : *this->instance->unitClauses) {
                unitLiteral = unitClause;
                break;
            }

            this->instance->unitClauses->erase(unitLiteral);
            propagateUnitClause(unitLiteral);
            return dpllInternal();
        }
    } catch (...) {
        return new DPLLResult(this->instance, this->model, false);
    }


    try {
        int branchVariable = this->branchingStrategy->pickBranchingVariable(this->instance);

        // POSITIVE ASSUMPTION
        // 1. make new entries on the stack
        this->removedClauseStack->push_back(new std::unordered_set<int>());
        this->removedLiteralStack->push_back(new std::unordered_map<int, std::unordered_set<int>*>());
        this->assignmentStack->push_back(new std::unordered_set<int>());

        // 2. mark the branching variable as a unit clause that needs to be 
        this->instance->unitClauses->insert(branchVariable);
                
        // 3. recurse with posiitive assumption
        DPLLResult* positiveAssumptionResult = dpllInternal();
        if (positiveAssumptionResult->isSAT) {
            return positiveAssumptionResult;
        }
        delete(positiveAssumptionResult);

        // BACKTRACKING -- undo effects of positive assumption
        backtrack();
        
        // NEGATIVE ASSUMPTION
        // 1. mark the negated branching variable as a unit clause that needs to be propagated
        this->instance->unitClauses->insert(-branchVariable);

        // 2. recurse with negative assumption 
        return dpllInternal();
    } catch (const std::exception& e) {
        std::cerr << "Exception caught: " << e.what() << std::endl;
        return NULL;
    }
}

DPLLResult* DPLL::dpll() {
    if (initiallyHasEmptyClause()) {
        return new DPLLResult(this->instance, this->model, false);
    }

    // std::cout << "checked for empty clause" << std::endl;
    
    // initialize stacks with empty elements (these initial elements should always remain on the stack -- never used
    // while backtracking etc since these are from before we ever branch)
    this->removedClauseStack->push_back(new std::unordered_set<int>());
    this->removedLiteralStack->push_back(new std::unordered_map<int, std::unordered_set<int>*>());
    this->assignmentStack->push_back(new std::unordered_set<int>());
    // std::cout << "created stacks" << std::endl;
    
    // populate remaining clauses
    for (int i = 0; i < this->instance->clauses->size(); i++) {
        this->remainingClauses->insert(i);
    }
    assert(this->remainingClauses->size() == this->instance->clauses->size());
    // std::cout << "populated remainingClauses" << std::endl;

    // finding pure symbols
    for (std::unordered_set<int> *clause : *this->instance->clauses) {
        for (int literal : *clause) {
            // int literalCount = getOrDefault(this->instance->literalCounts, literal, 0);
            this->instance->increaseLiteralCount(literal);
            // (*this->instance->literalCounts)[literal] = literalCount + 1;
            // if (mapContainsKey(this->instance->literalCounts, -literal)) {
            if (this->instance->getLiteralCounts(literal)->at(getVar(literal) - 1) > 0) {
                this->instance->pureSymbols->erase(literal);
                this->instance->pureSymbols->erase(-literal);
            } else {
                this->instance->pureSymbols->insert(literal);
            }
        }
    }
    // std::cout << "found pure symbols" << std::endl;

    propagatePureSymbols();
    // this->instance->pureSymbols->clear();
    // std::cout << "propagated pure symbols" << std::endl;

    findInitialUnitClauses();
    // std::cout << "found unit clauses" << std::endl;
    
    return dpllInternal();
}