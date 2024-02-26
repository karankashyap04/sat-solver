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

int ClauseReducer::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    MaxO maxo;
    maxo.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int maxoLiteral = maxo.pickBranchingVariable(instance);
    
    Moms moms;
    moms.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int momsLiteral = moms.pickBranchingVariable(instance);

    // if (this->remainingClauses->size() < 100) {
    //     std::random_device rd;  // Obtain a random number from hardware
    //     std::mt19937 rng(rd());
    //     std::uniform_int_distribution<int> distribution(1, 10);
    //     int random_int = distribution(rng);
    //     if (random_int % 2 == 0)
    //         return maxoLiteral;
    //     return momsLiteral;
    // }

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