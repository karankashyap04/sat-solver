#include <stdexcept>
#include <cmath>
#include "../include/MaxoMomsJwCR.hpp"
#include "../include/MaxO.hpp"
#include "../include/Moms.hpp"
#include "../include/Jw.hpp"
#include "../include/utils.hpp"

void MaxoMomsJwCR::setContext(std::unordered_set<int> *remainingClauses,
                               std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) {
    this->remainingClauses = remainingClauses;
    this->globalRemovedLiterals = globalRemovedLiterals;
}

/**
 * Picks one of three options: the most occurring variable (MAXO), and the most
 * occurring variable in clauses of minimal size (MOMS), and the variable predicted
 * by the Jeroslaw-Wang (JW) heuristic.
 * It picks between them based on the variable that would create the most
 * unit clauses in the branch for the negated literal of the variable.
*/
int MaxoMomsJwCR::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    MaxO maxo;
    maxo.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int maxoLiteral = maxo.pickBranchingVariable(instance);

    Moms moms;
    moms.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int momsLiteral = moms.pickBranchingVariable(instance);

    Jw jw;
    jw.setContext(this->remainingClauses, this->globalRemovedLiterals);
    int jwLiteral = jw.pickBranchingVariable(instance);

    int MAXO = 0, MOMS = 1, JW = 2;
    std::unordered_map<int, int> strategyLiterals;
    strategyLiterals[MAXO] = maxoLiteral;
    strategyLiterals[MOMS] = momsLiteral;
    strategyLiterals[JW] = jwLiteral;

    int maxUP = 0;
    int bestStrategy = MAXO;

    int maxoUP = 0, momsUP = 0, jwUP = 0;
    
    std::unordered_set<int> empty;
    for (int clauseIdx : *this->remainingClauses) {
        std::unordered_set<int> *clause = instance->clauses->at(clauseIdx);
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->globalRemovedLiterals, clauseIdx, &empty);
        if (clause->size() - clauseRemovedLiterals->size() == 2) {
            if (setContains(clause, maxoLiteral) && !setContains(clauseRemovedLiterals, maxoLiteral)) {
                maxoUP++;
                if (maxoUP >= maxUP) {
                    bestStrategy = MAXO;
                    maxUP = maxoUP;
                }
            }
            if (setContains(clause, momsLiteral) && !setContains(clauseRemovedLiterals, momsLiteral)) {
                momsUP++;
                if (momsUP > maxUP) {
                    bestStrategy = MOMS;
                    maxUP = momsUP;
                }
            }
            if (setContains(clause, jwLiteral) && !setContains(clauseRemovedLiterals, jwLiteral)) {
                jwUP++;
                if (jwUP > maxUP) {
                    bestStrategy = JW;
                    maxUP = jwUP;
                }
            }
        }
    }

    return strategyLiterals[bestStrategy];
}