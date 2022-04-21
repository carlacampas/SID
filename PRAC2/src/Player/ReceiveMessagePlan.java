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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveMessagePlan extends AbstractPlanBody {
  @Override
  public void action() {
    BeliefBase bb = getBeliefBase();
    Map<String, History> history = (Map<String, History>) bb.getBelief("history").getValue();
    int penalization = (int) bb.getBelief("penalization").getValue();
    int[] c = (int[]) (bb.getBelief("C").getValue());
    int[] d = (int[]) (bb.getBelief("D").getValue());
    
    ReceiveMessageGoal rg = (ReceiveMessageGoal) getGoal();
    Agent a = rg.getAgent();

    MessageTemplate tpl = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchOntology("play"));
    // Cyclic behaviour
    ACLMessage msg = a.receive(tpl);
    if (msg != null) {
      String content = msg.getContent(), name = msg.getSender().getName();
      if (content != null && (content.equals("C") || content.equals("D"))) {
        if (msg.getConversationId().equals(a.getAID().getName())) {
          History h = history.get(name);
          if (h.getSelection() == "C") {
            if (content == "C") penalization += c[0];  //CC
            else penalization += c[1];  //CD
          } else {
            if (content == "C") penalization += d[0];  //DC
            else penalization += d[1];  //DD
          }
          bb.updateBelief("penalization", penalization);
          dispatchGoal(new MinimizePlayGoal(rg.getAgent(), msg.getSender(), msg));
        } else dispatchGoal(new ChoosePlayGoal(content, a, msg.getSender(), msg));
      }
    }
  }
}