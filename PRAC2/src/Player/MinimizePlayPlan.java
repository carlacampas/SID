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
    Map<String, ArrayList<Object>> history_container = (Map<String, ArrayList<Object>>) bb.getBelief("history").getValue();


    
    int c_min = Math.min(c[0], c[1]);
    int d_min = Math.min(d[0], d[1]);

    String ch = "";
    if (!history_container.containsKey(mpg.getPlayer().getName()))
      ch = "C";
    else {
      ArrayList<Object> hist_vect = history_container.get(mpg.getPlayer().getName());
      History hist = (History) hist_vect.get(0);
      ch = hist.getLastPlay();
    }
    ArrayList<Object> elems = new ArrayList<Object>();
    History h = new History(mpg.getPlayer(), ch);

    elems.add(h);
    elems.add(mpg.getMessage());
    history_container.put(mpg.getPlayer().getName(), elems);
    bb.updateBelief("historyReplies", history_container);

    System.out.println("paso por minimizePlay");

    setEndState(Plan.EndState.SUCCESSFUL);

    //dispatchGoal(new SendGoal(mpg.getAgent(), mpg.getPlayer(), ch, mpg.getMessage()));
  }
}