package solver.sat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Usage example: read a given cnf instance file to create
 * a simple sat instance object and print out its parameter fields.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java Main <cnf file>");
            return;
        }

        String input = args[0];
        Path path = Paths.get(input);
        String filename = path.getFileName().toString();

        Timer watch = new Timer();
        watch.start();

        SATInstance instance = DimacsParser.parseCNFFile(input);
        Set<Integer> vars = new HashSet<>(instance.vars);
        System.out.println(instance);

        // run DPLL
        DPLL SATSolver = new DPLL();
        DPLLResult result = SATSolver.dpll(instance, new Model(new HashSet<Integer>()));

        watch.stop();
        if (result.isSAT) {
            System.out.println("{\"Instance\": \""
                    + filename
                    + "\", \"Time\": "
                    + String.format("%.2f", watch.getTime())
                    + ", \"Result\": \""
                    + result.isSAT
                    + "\", \"Solution\": "
                    + result.createSolutionString(vars)
                    + "}");
        }
        else {
            System.out.println("{\"Instance\": \""
                    + filename
                    + "\", \"Time\": "
                    + String.format("%.2f", watch.getTime())
                    + ", \"Result\": \""
                    + result.isSAT
                    + "\"}");
        }

    }
}
