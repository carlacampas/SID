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

public class ChooseGamePlan extends AbstractPlanBody {
  @Override
  public void action() {
    BeliefBase bb = getBeliefBase();
    int[] c = (int[]) (bb.getBelief("C").getValue());
    int[] d = (int[]) (bb.getBelief("D").getValue());

    ChooseGameGoal cg = (ChooseGameGoal) getGoal();
    String choice = cg.getChoice();

    String my_choice = "D";
    if ((cg.equals("C") && c[0] <= d[0]) || (cg.equals("D") && c[1] > d[1]))
      my_choice = "C";

    System.out.println ("my_choice: " +  my_choice);
    setEndState(Plan.EndState.SUCCESSFUL);
    dispatchGoal(new SendGoal(cg.getAgent(), cg.getAgainst(), my_choice, cg.getMessage()));
  }
}