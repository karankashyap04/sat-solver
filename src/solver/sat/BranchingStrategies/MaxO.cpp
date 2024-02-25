#include <stdexcept>
#include "../include/MaxO.hpp"
#include "../include/utils.hpp"

void MaxO::setContext(std::unordered_set<int> *remainingClauses,
                               std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) {
    this->remainingClauses = remainingClauses;
    this->globalRemovedLiterals = globalRemovedLiterals;
}

int MaxO::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    int maxOccurrenceVar = 0;
    int maxOccurrenceVarScore = 0;

    for (const auto& pair : *instance->literalCounts) {
        int literal = pair.first;
        int score = getOrDefault(instance->literalCounts, literal, 0) + getOrDefault(instance->literalCounts, -literal, 0);
        if (maxOccurrenceVarScore == 0 || score > maxOccurrenceVarScore) {
            maxOccurrenceVar = literal < 0 ? -literal : literal;
            maxOccurrenceVarScore = score;
        }
    }

    if (getOrDefault(instance->literalCounts, maxOccurrenceVar, 0) >= getOrDefault(instance->literalCounts, -maxOccurrenceVar, 0)) {
        return maxOccurrenceVar;
    }
    return -maxOccurrenceVar;
}