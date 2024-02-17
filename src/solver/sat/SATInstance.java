package solver.sat;

import java.util.*;

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

    public Map<Integer, Integer> literalCounts = new HashMap<>();

    public Set<Integer> pureSymbols = new HashSet<>();

    public Set<Integer> unitClauses = new HashSet<>();

    public SATInstance(int numVars, int numClauses) {
        this.numVars = numVars;
        this.numClauses = numClauses;
    }

    void addVariable(Integer literal) {
        vars.add((literal < 0) ? -1 * literal : literal);
    }

    void addClause(Set<Integer> clause) {
        clauses.add(clause);
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

    public void reduceLiteralCount(Integer literal) {
        Integer literalCount = this.literalCounts.getOrDefault(literal, 0);
        if (literalCount == 0) {
            System.out.println("ERROR: tried to reduce literal count for a variable with count 0");
            return;
        }
        if (literalCount == 1)
            this.literalCounts.remove(literal);
        else
            this.literalCounts.put(literal, literalCount - 1);
    }

    public void increaseLiteralCount(Integer literal) {
        Integer literalCount = this.literalCounts.getOrDefault(literal, 0);
        this.literalCounts.put(literal, 1 + literalCount);
    }

//    public SATInstance copy() {
//        SATInstance result = new SATInstance(this.numVars, this.numClauses);
//        result.vars = new HashSet<>(this.vars);
//        result.clauses = new ArrayList<Set<Integer>>();
//        for (Set<Integer> clause : this.clauses) {
//            result.clauses.add(new HashSet<>(clause));
//        }
//        result.literalCounts = new HashMap<>(this.literalCounts);
//        result.pureSymbols = new HashSet<>(this.pureSymbols);
//        result.unitClauses = new HashSet<>(this.unitClauses);
//        return result;
//    }

    public int getNumVars() {
        return this.numVars;
    }

    public int getNumClauses() { return this.numClauses; }
}
