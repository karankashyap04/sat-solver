package solver.sat;

import java.util.HashSet;
import java.util.Set;

public class Model {
     public Set<Integer> model = new HashSet<>();

     public Model(HashSet<Integer> model) {
          this.model = model;
     }
}
