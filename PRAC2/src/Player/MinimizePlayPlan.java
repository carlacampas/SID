package examples.Player;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;
import jade.core.AID;

public class MinimizePlayPlan extends AbstractPlanBody {
  @Override
  public void action() {
    BeliefBase bb = getBeliefBase();

    int[] c = (int[]) (bb.getBelief("C").getValue());
    int[] d = (int[]) (bb.getBelief("D").getValue());
    Set<String> history = (Set<String>) bb.getBelief("history").getValue();

    int c_min = Math.min(c[0], c[1]);
    int d_min = Math.min(d[0], d[1]);

    String ch = "D";
    if (c_min <= d_min) ch = "C";
    System.out.println(ch);

    history.add(ch);

    bb.updateBelief("history", history);
    System.out.println(bb.getBelief("history").getValue().toString());

    setEndState(Plan.EndState.SUCCESSFUL);
    bb.removeBelief("AID");
    //MinimizePlayGoal goal = (MinimizePlayGoal) getGoal();
  }
}