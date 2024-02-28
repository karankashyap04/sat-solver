#ifndef DIMACSPARSER_H
#define DIMACSPARSER_H

#include "SATInstance.hpp"
#include <stdexcept>
#include <iostream>
#include <string>

class DimacsParser 
{

public:
    static SATInstance* parseCNFFile(std::string filename);
};

#endif