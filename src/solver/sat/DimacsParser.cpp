#include "include/DimacsParser.hpp"
#include <fstream>
#include <sstream>
#include <unordered_set>

/**
 * Credit: some of the code for reading from files is based on something we read here:
 * https://stackoverflow.com/questions/7868936/read-file-line-by-line-using-ifstream-in-c
*/
SATInstance* DimacsParser::parseCNFFile(std::string filename) {
    SATInstance* satInstance = new SATInstance();

    try {
        std::string line;
        std::ifstream infile(filename);
        
        while (std::getline(infile, line)) {
            if (line[0] != 'c')
                break;
        }

        std::vector<std::string> tokens;
        std::string token;
        std::istringstream iss(line);
        while (std::getline(iss, token, ' ')) {
            tokens.push_back(token);
        }

        if (tokens[0] != "p")
            throw std::invalid_argument("Error: DIMACS file does not have problem line");
        if (tokens[1] != "cnf") {
            std::cout << "Error: DIMACS file format is not cnf" << std::endl;
            return satInstance;
        }

        int numVars = std::stoi(tokens[2]);
        int numClauses = std::stoi(tokens[3]);
        
        satInstance->numVars = numVars;
        satInstance->numClauses = numClauses;

        while (std::getline(infile, line)) {
            std::unordered_set<int> *clause = new std::unordered_set<int>();
            if (line[line.length() - 1] != '0')
                throw std::invalid_argument("Error: clause line does not end with 0");
            std::istringstream iss(line);
            while (std::getline(iss, token, ' ')) {
                if (token == "0")
                    break;
                if (token == "c")
                    continue;
                if (token == "")
                    continue;

                int literal = std::stoi(token);
                clause->insert(literal);
            }

            satInstance->addClause(clause);
        }
    }
    catch (...) {
        throw std::runtime_error("Error: DIMACS file is not found " + filename);
    }
    
    return satInstance;
}