#include "../include/Moms.hpp"

#include <stdexcept>
#include <limits>
#include "../include/utils.hpp"


void Moms::setContext(std::unordered_set<int> *remainingClauses,
                               std::unordered_map<int, std::unordered_set<int>*> *globalRemovedLiterals) {
    this->remainingClauses = remainingClauses;
    this->globalRemovedLiterals = globalRemovedLiterals;
}

std::unordered_set<int>* Moms::getMinSizeClauses(SATInstance *instance) {
    // returns the indices of the clauses of minimum size
    std::unordered_map<int, std::unordered_set<int>*> clausesOfSize;
    int currMinSize = std::numeric_limits<int>::max();

    for (int clauseIdx : *this->remainingClauses) {
        std::unordered_set<int> *clause = instance->clauses->at(clauseIdx);
        std::unordered_set<int> empty;
        int size = clause->size() - getOrDefault(this->globalRemovedLiterals, clauseIdx, &empty)->size();
        if (!mapContainsKey(&clausesOfSize, size)) {
            clausesOfSize[size] = new std::unordered_set<int>();
        }
        clausesOfSize[size]->insert(clauseIdx);
        if (size < currMinSize) {
            currMinSize = size;
        }
    }

    for (const auto& pair : clausesOfSize) {
        if (pair.first == currMinSize)
            continue;
        delete(pair.second);
    }

    return clausesOfSize[currMinSize];
}

/**
 * Picks the maximally occurring variable (across the positive and negative literals
 * that correspond to the variable) within clauses of the smallest size
*/
int Moms::pickBranchingVariable(SATInstance *instance) {
    if (this->remainingClauses->empty()) {
        throw std::runtime_error("Tried to pick branching variable with no clauses - already SAT");
    }

    std::unordered_set<int> *minSizeClauses = this->getMinSizeClauses(instance);

    std::unordered_map<int, int> literalScores;
    int maxOccurrences = 0;
    int maxVar = 0;

    std::unordered_set<int> empty;
    for (int clauseIdx : *minSizeClauses) {
        std::unordered_set<int> *clause = instance->clauses->at(clauseIdx);
        std::unordered_set<int> *clauseRemovedLiterals = getOrDefault(this->globalRemovedLiterals, clauseIdx, &empty);

        for (int literal : *clause) {
            if (setContains(clauseRemovedLiterals, literal))
                continue;
            literalScores[literal] = 1 + getOrDefault(&literalScores, literal, 0);

            int var = literal < 0 ? -literal : literal;
            int varScore = getOrDefault(&literalScores, literal, 0) + getOrDefault(&literalScores, -literal, 0);
            if (varScore > maxOccurrences) {
                maxOccurrences = varScore;
                maxVar = var;
            }
        }
    }

    delete(minSizeClauses);

    // if var occurs more than -var, we will first branch on var, and vice-versa
    if (getOrDefault(&literalScores, maxVar, 0) >= getOrDefault(&literalScores, -maxVar, 0)) {
        return maxVar;
    }

    return -maxVar;
}