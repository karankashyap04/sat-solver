package solver.sat.Checker;

import solver.sat.SATInstance;
import java.util.*;

public class DPLLChecker {
    public void check(SATInstance instance, String assignments, String name) {
        // populate the assignments
        Set<Integer> assignmentSet = new HashSet<>();
        String[] assignmentArray = assignments.split(" ");

        for (int i = 0; i < assignmentArray.length; i += 2) {
            if (assignmentArray[i + 1].equals("true")) {
                assignmentSet.add(Integer.parseInt(assignmentArray[i]));
            }
            else {
                assignmentSet.add(-1 * Integer.parseInt(assignmentArray[i]));
            }
        }

        for (Set<Integer> clause : instance.clauses) {
            boolean clauseBoolean = false;
            for (Integer literal : clause) {
                if (assignmentSet.contains(literal)) {
                    clauseBoolean = true;
                    break;
                }
            }

            if (!clauseBoolean) {
                System.out.println(name
                        + " was not satisfied by the assignment due to the following clause:\n"
                        + clause);
                return;
            }
        }

        System.out.println(name + " was satisfied by the assignment.");
    }
}
