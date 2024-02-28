#ifndef DPLL_H
#define DPLL_H

#include "BranchingStrategy.hpp"
#include "SATInstance.hpp"
#include "Model.hpp"
#include "DPLLResult.hpp"
#include <unordered_set>
#include <unordered_map>
#include <vector>

class DPLL
{
public:
    BranchingStrategy* branchingStrategy;
    SATInstance* instance;
    Model* model;
    
    std::unordered_set<int>* remainingClauses;
    std::unordered_map<int, std::unordered_set<int>*>* removedLiterals;
    std::vector<std::unordered_set<int>*>* removedClauseStack;
    std::vector<std::unordered_map<int, std::unordered_set<int>*>*>* removedLiteralStack;
    std::vector<std::unordered_set<int>*>* assignmentStack;

    DPLL(BranchingStrategy* branchingStrategy, SATInstance* insatnce, Model* model);

    DPLLResult* dpll();

private:
    void propagatePureSymbols();
    void propagateUnitClause(int literal);
    bool isSAT();
    bool initiallyHasEmptyClause();
    void findInitialUnitClauses();
    void backtrack();
    DPLLResult* dpllInternal();
};

#endif