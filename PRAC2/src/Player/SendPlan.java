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
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class SendPlan extends AbstractPlanBody {
  @Override
  public void action() {
    SendGoal sg = (SendGoal) getGoal();

    BeliefBase bb = getBeliefBase();
    int penalization = (int) bb.getBelief("penalization").getValue();
    System.out.println(sg.getAgent().getAID().getName() + " " + penalization);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    String choice = sg.getChoice();
    AID player = sg.getPlayer();
    Agent a = sg.getAgent();

    if (sg.getMessage() != null)
      msg = sg.getMessage().createReply();
    else {
      msg.setConversationId(a.getAID().getName());
    }

    msg.setContent(choice);
    msg.setOntology("play");
    msg.addReceiver(player);
    
    try {
      a.send(msg);
      setEndState(Plan.EndState.SUCCESSFUL);
    } catch (Exception e) {
      setEndState(Plan.EndState.FAILED);
    }
    
  }
}