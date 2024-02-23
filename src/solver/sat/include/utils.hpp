#include <unordered_map>

template <class T1, class T2>
T2 getOrDefault(std::unordered_map<T1, T2> *map, T1 key, T2 defaultVal) {
    typename std::unordered_map<T1, T2>::const_iterator search = map->find(key); 
    if (search != map->end()) {
        return search->second;
    }
    return defaultVal;
}