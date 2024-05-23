# sat-solver

An iterative DPLL [SAT solver](https://en.wikipedia.org/wiki/SAT_solver#:~:text=On%20input%20a%20formula%20over,values%20of%20x%20and%20y.) implemented in C++.

For information on how we built this SAT solver, our custom variable selection heuristic (`ClauseReducer`), optimizations made, and more, read our [report](https://github.com/karankashyap04/sat-solver/blob/main/report.pdf).

All the code for our solver is within the [`src/solver/sat`](https://github.com/karankashyap04/sat-solver/tree/main/src/solver/sat) subdirectory.

## Usage

Initially, you need to compile the code into a binary. To do this, run
```
./compile.sh
```

After this, to run the SAT solver on an individual input, run
```bash
./run.sh <input-file>
```
* For example, to solve the instance in the `input/C140.cnf` file, you would run:
  ```bash
  ./run.sh input/C140.cnf
  ```

> **Note**: The input files are expected to be in the [DIMACS CNF format](https://jix.github.io/varisat/manual/0.2.0/formats/dimacs.html). All the instances in the `input/` and `toy_inputs/` directories can serve as examples.

If you want to run the solver on all the input files in a directory, you can run
```bash
./runAll.sh <input-folder> <timeout (in seconds)> <output-filename>
```
* For example, to generate the [results.log](https://github.com/karankashyap04/sat-solver/blob/main/results.log) file containing solver results on all the instances in the `input/` directory with a 300 second timeout (per instance), we ran
  ```bash
  ./runAll.sh input/ 300 results.log
  ```

## Developers

This SAT Solver was built by [Karan Kashyap](https://github.com/karankashyap04) and [Erica Song](https://github.com/20songe) for Brown University's Prescriptive Analytics course.
