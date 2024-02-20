#include <chrono>

class Timer {
private:
    std::chrono::high_resolution_clock::time_point startTime;
    std::chrono::high_resolution_clock::time_point stopTime;
    bool running;
public:
    Timer() {
        running = false;
    }

    void reset() {
        running = false;
    }

    void start() {
        startTime = std::chrono::high_resolution_clock::now();
        running = true;
    }

    void stop() {
        if (running) {
            stopTime = std::chrono::high_resolution_clock::now();
            running = false;
        }
    }

    double getTime() {
        std::chrono::duration<double> elapsed;
        if (running) {
            elapsed = std::chrono::duration_cast<std::chrono::duration<double>>(
                std::chrono::high_resolution_clock::now() - startTime);
        } else {
            elapsed = std::chrono::duration_cast<std::chrono::duration<double>>(
                stopTime - startTime);
        }
        return elapsed.count();
    }
};
