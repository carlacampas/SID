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
    ReceiveMessageGoal rg = (ReceiveMessageGoal) getGoal();
    Agent a = rg.getAgent();
    BeliefBase bb = getBeliefBase();
    Set<String> started = (Set<String>) (bb.getBelief("started").getValue());

    MessageTemplate tpl = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchOntology("play"));
    // Cyclic behaviour
    ACLMessage msg = a.receive(tpl);
    if (msg != null) {
      String name = msg.getSender().getName();
      if (started.contains(name)) {
        dispatchGoal(new MinimizePlayGoal(rg.getAgent(), msg.getSender()));
        // count points, continue playing
      }
      else {
        Set<String> senders = (Set<String>) (bb.getBelief("plays").getValue());
        String content = msg.getContent();
        if (content != null && (content.equals("C") || content.equals("D"))) {
          senders.add(name);
          bb.updateBelief("plays", senders);
          dispatchGoal(new ChooseGameGoal(content, a, msg.getSender()));
        }
      }
    }
  }
}