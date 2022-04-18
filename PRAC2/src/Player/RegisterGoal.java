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
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.core.Agent;

public class RegisterGoal implements Goal {
  private Agent my_agent;

  public RegisterGoal(Agent a) {

    my_agent = a;

  }

  public Agent getAgent() { return my_agent; }

  public boolean hasAgent() {

    try {
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.setName(my_agent.getAID());
      DFAgentDescription[] results = DFService.search(my_agent, dfd);
      if (results.length == 1) return true;
      return false;
    }
    catch (Exception e) { return false; }

  }
}