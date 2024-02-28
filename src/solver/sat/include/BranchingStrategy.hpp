#ifndef BRANCHINGSTRATEGY_H
#define BRANCHINGSTRATEGY_H

#include <unordered_set>
#include <unordered_map>
#include "SATInstance.hpp"

// this class will be implemented by all the branching strategies
class BranchingStrategy
{
public:
    virtual int pickBranchingVariable(SATInstance *instance) = 0;
    virtual void setContext(std::unordered_set<int> *remainingClauses, 
                    std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) = 0;
};

#endif