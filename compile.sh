#!/bin/bash

########################################
############# CSCI 2951-O ##############
########################################

# Update this file with instructions on how to compile your code
g++ -O3 -w src/solver/sat/main.cpp src/solver/sat/include/main.hpp src/solver/sat/Timer.cpp \
    src/solver/sat/include/Timer.hpp src/solver/sat/SATInstance.cpp src/solver/sat/include/SATInstance.hpp \
    src/solver/sat/include/utils.hpp \
    src/solver/sat/DimacsParser.cpp src/solver/sat/include/DimacsParser.hpp \
    src/solver/sat/DPLLResult.cpp src/solver/sat/include/DPLLResult.hpp \
    src/solver/sat/Model.cpp src/solver/sat/include/Model.hpp \
    src/solver/sat/DPLL.cpp src/solver/sat/include/DPLL.hpp \
    src/solver/sat/BranchingStrategies/ClauseReducer.cpp src/solver/sat/BranchingStrategies/MaxO.cpp \
    src/solver/sat/include/ClauseReducer.hpp src/solver/sat/include/MaxO.hpp \
    src/solver/sat/BranchingStrategies/Moms.cpp src/solver/sat/include/Moms.hpp \
    src/solver/sat/BranchingStrategies/Jw.cpp src/solver/sat/include/Jw.hpp \
    src/solver/sat/BranchingStrategies/SUP.cpp src/solver/sat/include/SUP.hpp \
    src/solver/sat/BranchingStrategies/MaxoMomsJwCR.cpp src/solver/sat/include/MaxoMomsJwCR.hpp