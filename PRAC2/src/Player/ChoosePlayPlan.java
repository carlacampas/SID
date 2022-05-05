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
import jade.domain.FIPAException;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;


public class ChoosePlayPlan extends AbstractPlanBody {
  @Override
  public void action() {
    BeliefBase bb = getBeliefBase();
    int[] c = (int[]) (bb.getBelief("C").getValue());
    int[] d = (int[]) (bb.getBelief("D").getValue());
    int penalization = (int) bb.getBelief("penalization").getValue();

    examples.Player.ChoosePlayGoal cg = (examples.Player.ChoosePlayGoal) getGoal();
    //String choice = cg.getChoice();

    LinkedList<QueueElem> queue = (LinkedList<QueueElem>) bb.getBelief("replyQueue").getValue();



    String my_choice = "D";
    if (cg.equals("C")) {
      if (c[0] <= d[0]) {
        my_choice = "C";
        penalization += c[0];
      } else penalization += d[0];
    }
    else {
      if (c[1] > d[1]) {
        my_choice = "C";
        penalization += c[1];
      } else penalization += d[1];
    }
    //ArrayList<Object> elems = new ArrayList<Object>();

    History h = new History(cg.getAgainst(), my_choice);

    QueueElem qelem = new QueueElem(h, cg.getMessage());
    //elems.add(h);
    //elems.add(cg.getMessage());
    queue.add(qelem);
    bb.updateBelief("replyQueue", queue);

    //System.out.println ("my_choice: " +  my_choice);
    bb.updateBelief("penalization", penalization);
    setEndState(Plan.EndState.SUCCESSFUL);
    //dispatchGoal(new SendGoal(cg.getAgent(), cg.getAgainst(), my_choice, cg.getMessage()));
  }
}