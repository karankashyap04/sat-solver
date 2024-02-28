#include "include/SATInstance.hpp"
#include "include/utils.hpp"
#include <iostream>

SATInstance::SATInstance() {
    this->numVars = 0;
    this->numClauses = 0;

    this->vars = new std::unordered_set<int>();
    this->clauses = new std::vector<std::unordered_set<int>*>();
    this->pureSymbols = new std::unordered_set<int>();
    this->unitClauses = new std::unordered_set<int>();
}

std::vector<int>* SATInstance::getLiteralCounts(int literal) {
    // returns positive or negative literal counts, as appropriate for the
    // literal provided
    return literal > 0 ? this->positiveLiteralCounts : this->negativeLiteralCounts;
}

void SATInstance::instantiateLiteralCounts() {
    this->positiveLiteralCounts = new std::vector<int>(this->numVars, 0);
    this->negativeLiteralCounts = new std::vector<int>(this->numVars, 0);
}

void SATInstance::addVariable(int literal) {
    vars->insert(literal < 0 ? -literal : literal);
}

void SATInstance::addClause(std::unordered_set<int> *clause) {
    clauses->push_back(clause);
}

void SATInstance::reduceLiteralCount(int literal) {
    std::vector<int> *literalCounts = this->getLiteralCounts(literal);
    int literalIdx = (literal < 0 ? -literal : literal) - 1;
    int literalCount = literalCounts->at(literalIdx);
    if (literalCount == 0) {
        std::cout << "ERROR: tried to reduce literal count for a variable with count 0" << std::endl;
        return;
    }
    literalCounts->at(literalIdx) -= 1;
}

void SATInstance::increaseLiteralCount(int literal) {
    std::vector<int> *literalCounts = this->getLiteralCounts(literal);
    int literalIdx = (literal < 0 ? -literal : literal) - 1;
    literalCounts->at(literalIdx) += 1;
}