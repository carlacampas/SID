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
    Agent a = sg.getAgent();
     
 
    Set<AID> ag = (Set<AID>) this.getBeliefBase().getBelief("games").getValue();
    for(AID r : ag){
      try { // envia un missatge un missatge en cas que no estigui dins el rang
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("Play " + sg.getPlays());
        msg.setOntology("play");
        msg.addReceiver(r);
        System.out.println("Message from " + a.getName() + " to " + r.getName() + " sent sucessfully");
        a.send(msg);
      } catch (Exception e) { setEndState(Plan.EndState.FAILED); }
    }
    sg.inc_plays();
    if(sg.getPlays()==5) setEndState(Plan.EndState.SUCCESSFUL);
  }
}