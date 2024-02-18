package solver.sat.Checker;

import solver.sat.*;
import solver.sat.BranchingStrategies.BranchingStrategy;
import solver.sat.BranchingStrategies.MaxOccurrences;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import java.io.BufferedReader;
import java.io.FileReader;

public class CheckerMain {
    public static void main(String[] args) throws Exception {

        DPLLChecker checker = new DPLLChecker();

        // read results.log
        String resultsPath = "resultsBacktrackMOMSMAXOSUP.log";
        BufferedReader reader = new BufferedReader(new FileReader(resultsPath));

        String line = reader.readLine();

        while (line != null) {

            String[] splitLine = line.split(", ");

            String filename = splitLine[0];
            filename = "input/" + filename.substring(filename.indexOf(":") + 3, filename.length() - 1);

            SATInstance instance = DimacsParser.parseCNFFile(filename);

            if (splitLine.length  == 4) {
                String solution = splitLine[3];
                String assignment = solution.substring(solution.indexOf(":") + 3, solution.length() - 2);
                checker.check(instance, assignment, filename);
            }

            line = reader.readLine();
        }

        reader.close();
    }
}
