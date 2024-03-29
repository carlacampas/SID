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

public class RegisterPlan extends AbstractPlanBody {
  @Override
  public void action() {
    RegisterGoal rg = (RegisterGoal) getGoal();
    Agent a = rg.getAgent();

    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("player");
    sd.setName(a.getName());
    dfd.setName(a.getAID());
    dfd.addServices(sd);

    try {
      DFService.register(a,dfd);
    } catch (FIPAException e) {
      setEndState(Plan.EndState.FAILED);
    }

    if (rg.hasAgent()) {
      System.out.println("agent sucessfully registered in platform");
      setEndState(Plan.EndState.SUCCESSFUL);
      dispatchGoal(new ReceiveMessageGoal(a));
      dispatchGoal(new FindGoal(a));
    }
  }
}