package solver.sat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple class to represent a SAT instance.
 */
public class SATInstance {
    // The number of variables
    int numVars;

    // The number of clauses
    int numClauses;

    // The set of variables (variables are strictly positive integers)
    public Set<Integer> vars = new HashSet<Integer>();

    // The list of clauses
    public List<Set<Integer>> clauses = new ArrayList<Set<Integer>>();

    public SATInstance(int numVars, int numClauses) {
        this.numVars = numVars;
        this.numClauses = numClauses;
    }

    void addVariable(Integer literal) {
        vars.add((literal < 0) ? -1 * literal : literal);
        numVars ++;
    }

    void addClause(Set<Integer> clause) {
        clauses.add(clause);
        numClauses++;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Number of variables: " + numVars + "\n");
        buf.append("Number of clauses: " + numClauses + "\n");
        buf.append("Variables: " + vars.toString() + "\n");
        for (int c = 0; c < clauses.size(); c++)
            buf.append("Clause " + c + ": " + clauses.get(c).toString() + "\n");
        return buf.toString();
    }

    public SATInstance copy() {
        SATInstance result = new SATInstance(this.numVars, this.numClauses);
        result.vars = new HashSet<>(this.vars);
        result.clauses = new ArrayList<Set<Integer>>();
        for (Set<Integer> clause : this.clauses) {
            result.clauses.add(new HashSet<>(clause));
        }
        return result;
    }

    public int getNumVars() {
        return this.numVars;
    }

    public int getNumClauses() { return this.numClauses; }
}
