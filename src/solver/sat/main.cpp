#include <iostream>
#include <cmath>
#include "include/main.hpp"
#include "include/Timer.hpp"
#include "include/DimacsParser.hpp"
#include <string>

using namespace std;

int main(int argc, char *argv[]) {
    if (argc != 2) {
        cout << "Usage: Main <cnf file>" << endl;
        return 0;
    }

    std::string filename = argv[1];
    cout << "filename: " << filename << endl;
    
    Timer watch;
    watch.start();
    
    SATInstance* instance = DimacsParser::parseCNFFile(filename);
    int numClauses = instance->numClauses;
    int numVars = instance->numVars;
    
    cout << "Number of Clauses: " << numClauses << endl;
    cout << "Number of Variables: " << numVars << endl;
    watch.stop();

    float timeElapsed = floor(watch.getTime() * 100.0) / 100.0;

    cout << "time elapsed: " << timeElapsed << "s" << endl;

    return 0;
}
