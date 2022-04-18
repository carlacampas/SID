
package examples.Player;

import java.util.*;
import bdi4jade.goal.*;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.SubscriptionInitiator;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.*;
import jade.util.Logger;
import bdi4jade.goal.SequentialGoal;

public class Player extends SingleCapabilityAgent {
  Set<AID> seen = new HashSet <>();
  BeliefBase beliefBase;

  // Cyclic behaviour
  public class RecieveMessages extends CyclicBehaviour {
    MessageTemplate tpl;
    ACLMessage msg;

    public void onStart() {
      tpl = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    }

    // Si rep el missatge de "temperature" envia la seva temperatura actual
    public void action() {
      msg = myAgent.receive(tpl);
      if (msg != null) {
        String content = msg.getContent();
        if ((content != null) && (content.indexOf("new game") != -1)) {
          // add balief
          Belief aid = new TransientBelief("AID", msg.getSender());
          beliefBase.addBelief(aid);
          // add goal
          addGoal(new MinimizePlayGoal());
        }
      }
      else {
        block();
      }
    }
  }

  protected void init() {
    Object[] args = getArguments();
    if (args.length != 4) {
      System.out.println("incorrect args");
      doDelete();
    }

    int CC = Integer.parseInt(args[0].toString());
    int CD = Integer.parseInt(args[1].toString());
    int DC = Integer.parseInt(args[2].toString());
    int DD = Integer.parseInt(args[3].toString());
    
    Belief C = new TransientBelief("C", new int[] {CC, CD});
    Belief D = new TransientBelief("D", new int[] {DC, DD});
    Belief history = new TransientBeliefSet("history", new HashSet());

    Capability c = getCapability();
    beliefBase = c.getBeliefBase();

    beliefBase.addBelief(C);
    beliefBase.addBelief(D);
    beliefBase.addBelief(history);

    //Plan plan = new DefaultPlan(MinimizePlayGoal.class, MinimizePlayPlan.class);
    Plan reg = new DefaultPlan(RegisterGoal.class, RegisterPlan.class);
    Plan find_agents = new DefaultPlan(FindGoal.class, FindPlan.class);

    c.getPlanLibrary().addPlan(reg);
    c.getPlanLibrary().addPlan(find_agents);
    this.addGoal(new RegisterGoal(this));
  }
}