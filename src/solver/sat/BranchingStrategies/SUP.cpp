#include <stdexcept>
#include <cmath>
#include "../include/SUP.hpp"
#include "../include/MaxO.hpp"
#include "../include/Moms.hpp"
#include "../include/utils.hpp"

void SUP::setContext(std::unordered_set<int> *remainingClauses,
                               std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) {
    this->remainingClauses = remainingClauses;
    this->globalRemovedLiterals = globalRemovedLiterals;
}

/**
 * Picks the variable which will lead to the most unit propagations
*/
int SUP::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    MaxO maxo;
    maxo.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int maxoLiteral = maxo.pickBranchingVariable(instance);

    Moms moms;
    moms.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int momsLiteral = moms.pickBranchingVariable(instance);

    int maxoUP = 0, momsUP = 0;
    
    std::unordered_set<int> empty;
    for (int clauseIdx : *this->remainingClauses) {
        std::unordered_set<int> *clause = instance->clauses->at(clauseIdx);
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->globalRemovedLiterals, clauseIdx, &empty);
        if (clause->size() - clauseRemovedLiterals->size() == 2) {
            if (setContains(clause, -maxoLiteral) && !setContains(clause, maxoLiteral) && !setContains(clauseRemovedLiterals, -maxoLiteral)) {
                maxoUP++;
            }
            if (setContains(clause, -momsLiteral) && !setContains(clause, maxoLiteral) && !setContains(clauseRemovedLiterals, -momsLiteral)) {
                momsUP++;
            }
        }
    }

    if (maxoUP >= momsUP)
        return maxoLiteral;
    return momsLiteral;
}