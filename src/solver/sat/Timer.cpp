#include "include/Timer.hpp"

Timer::Timer() {
    running = false;
}

void Timer::reset() {
    running = false;
}

void Timer::start() {
    startTime = std::chrono::high_resolution_clock::now();
    running = true;
}

void Timer::stop() {
    if (running) {
        stopTime = std::chrono::high_resolution_clock::now();
        running = false;
    }
}

double Timer::getTime() {
    std::chrono::duration<double> elapsed;
    if (running) {
        elapsed = std::chrono::duration_cast<std::chrono::duration<double> >(
            std::chrono::high_resolution_clock::now() - startTime);
    } else {
        elapsed = std::chrono::duration_cast<std::chrono::duration<double> >(
            stopTime - startTime);
    }
    return elapsed.count();
}
