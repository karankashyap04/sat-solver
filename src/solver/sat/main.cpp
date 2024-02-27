#include <iostream>
#include <cmath>
#include <string>
#include "include/main.hpp"
#include "include/Timer.hpp"
#include "include/DimacsParser.hpp"
#include "include/BranchingStrategy.hpp"
#include "include/DPLL.hpp"
#include "include/ClauseReducer.hpp"
#include "include/Model.hpp"
// #include "include/SUP.hpp"
// #include "include/MaxoMomsJwCR.hpp"

using namespace std;

int main(int argc, char *argv[]) {
    if (argc != 2) {
        cout << "Usage: Main <cnf file>" << endl;
        return 0;
    }

    std::string input = argv[1];
    std::string filename;

    size_t lastIndex = input.find_last_of('/');
    if (lastIndex != std::string::npos) {
        filename = input.substr(lastIndex + 1, input.length() - lastIndex);
    }
    else {
        filename = input;
    }
    cout << "filename: " << filename << endl;
    
    Timer watch;
    watch.start();
    
    SATInstance* instance = DimacsParser::parseCNFFile(input);
    int numClauses = instance->numClauses;
    int numVars = instance->numVars;
    instance->instantiateLiteralCounts();
    
    cout << "Number of Clauses: " << numClauses << endl;
    cout << "Number of Variables: " << numVars << endl;

    BranchingStrategy *branchingStrategy = new ClauseReducer();
    Model model(new std::unordered_set<int>());
    DPLL *SATSolver = new DPLL(branchingStrategy, instance, &model);
    DPLLResult *result = SATSolver->dpll();

    watch.stop();

    // TODO: remove later -- this was just to ensure that clauses contained expected literals
    // for (size_t i = 0; i < instance->clauses->size(); i++) {
    //     auto clause = instance->clauses->at(i);
    //     for (auto it = clause->begin(); it != clause->end(); it++) {
    //         cout << *it << " ";
    //     }
    //     cout << endl;
    // }

    float timeElapsed = floor(watch.getTime() * 100.0) / 100.0;
    // float timeElapsed = watch.getTime();

    cout << "time elapsed: " << timeElapsed << "s" << endl;
    
    if (result->isSAT) {
        cout << "{\"Instance\": \"" << filename << "\", \"Time\": "
        << timeElapsed // round time to 2 decimal places
        << ", \"Result\": \"SAT\", \"Solution\": \""
        << result->createSolutionString(instance->vars) << "\"}" << endl;
    
    }
    else {
        cout << "{\"Instance\": \"" << filename << "\", \"Time\": "
        << timeElapsed // round time to 2 decimal places
        << ", \"Result\": \"UNSAT\"}" << endl;
    }

    delete(branchingStrategy);
    delete(model.model);
    delete(instance->vars);
    delete(instance->clauses);
    delete(instance->pureSymbols);
    delete(instance->unitClauses);
    delete(instance->positiveLiteralCounts);
    delete(instance->negativeLiteralCounts);
    delete(SATSolver);
    delete(result);

    return 0;
}
