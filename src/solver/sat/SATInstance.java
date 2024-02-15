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

    public TreeMap<Integer, Integer> sortedVarCounts = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2 - o1; // sort in decreasing order
        }
    });

    public Set<Integer> pureSymbols = new HashSet<>();

    public List<Integer> unitClauses = new ArrayList<>();

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
        if (literalCount == 0)
            return;
        if (literalCount == 1)
            this.literalCounts.remove(literal);
        else
            this.literalCounts.put(literal, literalCount - 1);
    }

    public void reduceVarCount(Integer literal) {
        Integer var = literal < 0 ? -literal : literal;
        int newVarScore = this.sortedVarCounts.remove(var) - 1;
        if (newVarScore > 0)
            this.sortedVarCounts.put(var, newVarScore);
    }

    public SATInstance copy() {
        SATInstance result = new SATInstance(this.numVars, this.numClauses);
        result.vars = new HashSet<>(this.vars);
        result.clauses = new ArrayList<Set<Integer>>();
        for (Set<Integer> clause : this.clauses) {
            result.clauses.add(new HashSet<>(clause));
        }
        result.literalCounts = new HashMap<>(this.literalCounts);
        result.pureSymbols = new HashSet<>(this.pureSymbols);
        result.unitClauses = new ArrayList<>(this.unitClauses);
        result.sortedVarCounts = (TreeMap<Integer, Integer>) this.sortedVarCounts.clone();
        return result;
    }

    public int getNumVars() {
        return this.numVars;
    }

    public int getNumClauses() { return this.numClauses; }
}
