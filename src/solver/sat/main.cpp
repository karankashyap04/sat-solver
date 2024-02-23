#include <iostream>
#include <cmath>
#include "include/main.hpp"
#include "include/Timer.hpp"

using namespace std;

int main(int argc, char *argv[]) {
    if (argc != 2) {
        cout << "Usage: Main <cnf file>" << endl;
        return 0;
    }

    char* filename = argv[1];
    
    Timer watch;
    watch.start();

    for (int i = 0; i < 100000000; i++) {}

    watch.stop();

    float timeElapsed = floor(watch.getTime() * 100.0) / 100.0;

    cout << "time elapsed: " << timeElapsed << "s" << endl;

    return 0;
}
