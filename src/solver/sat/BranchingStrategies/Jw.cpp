#include <stdexcept>
#include <cmath>
#include "../include/Jw.hpp"
#include "../include/utils.hpp"

void Jw::setContext(std::unordered_set<int> *remainingClauses,
                               std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) {
    this->remainingClauses = remainingClauses;
    this->globalRemovedLiterals = globalRemovedLiterals;
}

/**
 * Picks a variable based on the Jeroslaw-Wang heuristic
*/
int Jw::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    std::unordered_map<int, double> literalScores;
    int maxVar = 0;
    double maxScore = 0;
    std::unordered_set<int> empty;
        
    for (int clauseIdx : *this->remainingClauses) {
        std::unordered_set<int> *clause = instance->clauses->at(clauseIdx);
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->globalRemovedLiterals, clauseIdx, &empty);
        int clauseLength = clause->size() - clauseRemovedLiterals->size();
        double weight = std::pow(2, -clauseLength);
        for (int literal : * clause) {
            if (setContains(clauseRemovedLiterals, literal))
                continue;
            literalScores[literal] = weight + getOrDefault(&literalScores, -literal, 0.0);
            int var = literal < 0 ? -literal : literal;
            double varScore = getOrDefault(&literalScores, literal, 0.0) + getOrDefault(&literalScores, -literal, 0.0);
            if (varScore > maxScore) {
                maxScore = varScore;
                maxVar = var;
            }
        }
    }

    if (getOrDefault(&literalScores, maxVar, 0.0) >= getOrDefault(&literalScores, -maxVar, 0.0)) {
        return maxVar;
    }

    return -maxVar;
}