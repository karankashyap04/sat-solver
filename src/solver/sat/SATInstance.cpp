#include "include/SATInstance.hpp"
#include "include/utils.hpp"
#include <iostream>

SATInstance::SATInstance() {
    this->numVars = 0;
    this->numClauses = 0;

    this->vars = new std::unordered_set<int>();
    this->clauses = new std::vector<std::unordered_set<int>*>();
    this->literalCounts = new std::unordered_map<int, int>();
    this->pureSymbols = new std::unordered_set<int>();
    this->unitClauses = new std::unordered_set<int>();
}

void SATInstance::addVariable(int literal) {
    vars->insert(literal < 0 ? -literal : literal);
}

void SATInstance::addClause(std::unordered_set<int> *clause) {
    clauses->push_back(clause);
}

void SATInstance::reduceLiteralCount(int literal) {
    int literalCount = getOrDefault(literalCounts, literal, 0);
    if (literalCount == 0) {
        std::cout << "ERROR: tried to reduce literal count for a variable with count 0" << std::endl;
        return;
    }
    if (literalCount == 1) {
        literalCounts->erase(literal);
    } else {
        literalCounts->at(literal) = literalCount - 1;
    }
}

void SATInstance::increaseLiteralCount(int literal) {
    int literalCount = getOrDefault(literalCounts, literal, 0);
    if (literalCount == 0) {
        literalCounts->insert(std::make_pair(literal, 1));
    } else {
        literalCounts->at(literal) = literalCount + 1;
    }
}