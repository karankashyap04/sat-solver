#ifndef MAXOMOMSJWCR_H
#define MAXOMOMSJWCR_H

#include "BranchingStrategy.hpp"
#include "SATInstance.hpp"
#include <unordered_map>
#include <unordered_set>

class MaxoMomsJwCR : public BranchingStrategy {
public:
    // do something
    int pickBranchingVariable(SATInstance *instance) override;
    void setContext(std::unordered_set<int> *remainingClauses, 
                    std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) override;

private:
    std::unordered_set<int> *remainingClauses;
    std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals; 
};

#endif