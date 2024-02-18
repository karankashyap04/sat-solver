package solver.sat;

import java.util.Set;

public class DPLLResult {
    public SATInstance instance;
    public Model model;
    public boolean isSAT;

    public DPLLResult(SATInstance instance, Model model, boolean isSAT) {
        this.instance = instance;
        this.model = model;
        this.isSAT = isSAT;
    }

    public String createSolutionString(Set<Integer> vars) {
        StringBuffer buf = new StringBuffer();
        for (Integer var : vars) {
            if (this.model.model.contains(var)) {
                buf.append(var + " true ");
                if (this.model.model.contains(-var)) {
                    System.out.println("-var is there and var too!");
                }
            } else { // NOTE: assign false to arbitrary choice vars
                buf.append(var + " false ");
            }
        }
        String result = buf.toString();
        return result.substring(0, result.length() - 1);
    }
}
