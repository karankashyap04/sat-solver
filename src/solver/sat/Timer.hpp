#ifndef TIMER_H
#define TIMER_H

class Timer
{
private:
    std::chrono::high_resolution_clock::time_point startTime;
    std::chrono::high_resolution_clock::time_point stopTime;
    bool running;

public:
    Timer();

    void reset();
    void start();
    void stop();
    double get_time();
};

#endif