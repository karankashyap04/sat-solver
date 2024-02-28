#ifndef MODEL_H
#define MODEL_H

#include <unordered_set>

class Model
{
public:
    std::unordered_set<int>* model; // to store variable assignments

    Model(std::unordered_set<int>* model);
};

#endif