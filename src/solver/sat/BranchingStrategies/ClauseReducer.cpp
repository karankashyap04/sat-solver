#include <stdexcept>
#include <random>
#include "../include/ClauseReducer.hpp"
#include "../include/MaxO.hpp"
#include "../include/Moms.hpp"
#include "../include/utils.hpp"

void ClauseReducer::setContext(std::unordered_set<int> *remainingClauses,
                               std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) {
    this->remainingClauses = remainingClauses;
    this->globalRemovedLiterals = globalRemovedLiterals;
}

/**
 * Picks one of two options: the most occurring variable (MAXO), and the most
 * occurring variable in clauses of minimal size (MOMS). In picking between
 * the two, it compares how many unit propagations the MAXO variable would
 * create in this branch, and how many unit clauses the MOMS variable would
 * create in branch for the negated literal for the variable.
*/
int ClauseReducer::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    // get literal based on MAXO: most occurring var
    MaxO maxo;
    maxo.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int maxoLiteral = maxo.pickBranchingVariable(instance);
    
    // get literal based on MOMS: most occurring var in clauses of minimum size
    Moms moms;
    moms.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int momsLiteral = moms.pickBranchingVariable(instance);

    int MAXO = 0, MOMS = 1;
    int maxoUP = 0, momsUP = 0;

    std::unordered_set<int> empty;
    for (int clauseIdx : *this->remainingClauses) {
        std::unordered_set<int> *clause = instance->clauses->at(clauseIdx);
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->globalRemovedLiterals, clauseIdx, &empty);
        if (clause->size() - clauseRemovedLiterals->size() == 2) {
            if (setContains(clause, -maxoLiteral) && !setContains(clauseRemovedLiterals, -maxoLiteral)) {
                maxoUP++;
            }
            if (setContains(clause, momsLiteral) && !setContains(clauseRemovedLiterals, momsLiteral)) {
                momsUP++;
            }
        }
    }

    if (maxoUP >= momsUP)
        return maxoLiteral;
    return momsLiteral;
}