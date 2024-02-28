#ifndef SUP_H
#define SUP_H

#include "BranchingStrategy.hpp"
#include "SATInstance.hpp"
#include <unordered_map>
#include <unordered_set>

// Implements a variation of the SUP branching heuristic as per Lagoudakis, M.
// & Littman, M.
// (https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=5c33e9abec94c4093742f23683abd357d48991c6)
class SUP : public BranchingStrategy {
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