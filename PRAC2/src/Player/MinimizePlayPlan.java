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
    MinimizePlayGoal mpg = (MinimizePlayGoal) getGoal();
    BeliefBase bb = getBeliefBase();

    int[] c = (int[]) (bb.getBelief("C").getValue());
    int[] d = (int[]) (bb.getBelief("D").getValue());
    Set<String> plays = (Set<String>) (bb.getBelief("plays").getValue());
    Map<String, History> history = (Map<String, History>) bb.getBelief("history").getValue();
    
    int c_min = Math.min(c[0], c[1]);
    int d_min = Math.min(d[0], d[1]);

    String ch = "D";
    if (c_min <= d_min) ch = "C";

    History h = new History(mpg.getPlayer(), ch);
    history.put(mpg.getPlayer().getName(), h);
    bb.updateBelief("history", history);

    setEndState(Plan.EndState.SUCCESSFUL);

    dispatchGoal(new SendGoal(mpg.getAgent(), mpg.getPlayer(), ch, mpg.getMessage()));
  }
}