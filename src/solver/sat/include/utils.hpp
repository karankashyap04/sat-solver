#include <unordered_map>
#include <unordered_set>

template <class T1, class T2>
T2 getOrDefault(std::unordered_map<T1, T2> *map, T1 key, T2 defaultVal) {
    typename std::unordered_map<T1, T2>::const_iterator search = map->find(key); 
    if (search != map->end()) {
        return search->second;
    }
    return defaultVal;
}

template <class T1>
bool setContains(std::unordered_set<T1> *set, T1 toCheck) {
    auto it = set->find(toCheck);
    return it != set->end();
}