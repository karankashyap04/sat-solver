#ifndef SATINSTANCE_H
#define SATINSTANCE_H

#include <unordered_set>
#include <vector>
#include <unordered_map>

class SATInstance
{
public:
    int numVars;
    int numClauses;
    std::unordered_set<int> *vars;
    std::vector<std::unordered_set<int>*> *clauses;
    // std::unordered_map<int, int> *literalCounts;
    std::vector<int> *positiveLiteralCounts;
    std::vector<int> *negativeLiteralCounts;
    std::unordered_set<int> *pureSymbols;
    std::unordered_set<int> *unitClauses;

    // SATInstance(int numVars, int numClauses);
    SATInstance();

    void addVariable(int literal);
    void addClause(std::unordered_set<int> *clause);
    void instantiateLiteralCounts();
    std::vector<int>* getLiteralCounts(int literal); // returns the positive or negative literal counts as appropriate for the provided literal
    // NOTE: not implementing toString from Java implementation -- I don't think we need it 
    void reduceLiteralCount(int literal);
    void increaseLiteralCount(int literal);
};

#endif