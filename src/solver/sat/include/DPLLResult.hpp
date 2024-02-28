#ifndef DPLLRESULT_H
#define DPLLRESULT_H

#include "SATInstance.hpp"
#include "Model.hpp"
#include <string>

class DPLLResult
{
public:
    SATInstance* instance;
    Model* model;
    bool isSAT;

    DPLLResult(SATInstance* instance, Model* model, bool isSat);

    std::string createSolutionString(std::unordered_set<int>* vars);
};

#endif