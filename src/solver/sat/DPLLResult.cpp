#include "include/DPLLResult.hpp"
#include "include/utils.hpp"

DPLLResult::DPLLResult(SATInstance* instance, Model* model, bool isSat) {
    this->instance = instance;
    this->model = model;
    this->isSAT = isSat;
}

std::string DPLLResult::createSolutionString(std::unordered_set<int>* vars) {
    // generates the string of variable assignments that is mapped to by the
    // "Solution" key in the final result printed by the solver
    std::string result = "";
    for (int var : *vars) {
        if (setContains(this->model->model, var)) {
            result += std::to_string(var) + " true ";
        } else {
            result += std::to_string(var) + " false ";
        }
    }
    return result.substr(0, result.length() - 1);
}