#include <unordered_set>
#include <unordered_map>
#include "SATInstance.hpp"

class BranchingStrategy
{
public:
    virtual int pickBranchingVariable(SATInstance *instance) = 0;
    void setContext(std::unordered_set<int> *remainingClauses, 
                    std::unordered_map<int, std::unordered_set<int>*> globalRemovedLiterals);
    // NOTE: there were more functions here in the Java implementation; I left them out for now
    //       since I figured it would make sense to implement them only if we need them here
};