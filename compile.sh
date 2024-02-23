#!/bin/bash

########################################
############# CSCI 2951-O ##############
########################################

# Update this file with instructions on how to compile your code
g++ src/solver/sat/main.cpp src/solver/sat/include/main.hpp src/solver/sat/Timer.cpp \
    src/solver/sat/include/Timer.hpp src/solver/sat/SATInstance.cpp src/solver/sat/include/SATInstance.hpp \
    src/solver/sat/include/utils.hpp \
    src/solver/sat/DimacsParser.cpp src/solver/sat/include/DimacsParser.hpp