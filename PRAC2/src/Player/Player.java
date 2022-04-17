
package examples.Player;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.goal.GoalTemplate;
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

public class Player extends SingleCapabilityAgent {
  Set<AID> seen = new HashSet <>();

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

    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("player");
    sd.setName(getName());
    dfd.setName(getAID());
    dfd.addServices(sd);

    try {
      DFService.register(this,dfd);
    } catch (FIPAException e) {
      //myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
      doDelete();
    }

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("player");
    template.addServices(templateSd);
    //SearchConstraints sc = new SearchConstraints();
    //sc.setMaxResults(Long.valueOf(10));

    try { // envia un missatge un missatge en cas que no estigui dins el rang
      DFAgentDescription[] results = DFService.search(this, template, null);
      ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
      msg.setContent("new game");
      for (DFAgentDescription r : results) {
        msg.addReceiver(r.getName());
      }
      send(msg);
    } catch (Exception e) {}

    int CC = Integer.parseInt(args[0].toString());
    int CD = Integer.parseInt(args[1].toString());
    int DC = Integer.parseInt(args[2].toString());
    int DD = Integer.parseInt(args[3].toString());
    
    Belief C = new TransientBelief("C", new int[] {CC, CD});
    Belief D = new TransientBelief("D", new int[] {DC, DD});
    Belief history = new TransientBeliefSet("history", new HashSet());

    Capability c = getCapability();
    BeliefBase beliefBase = c.getBeliefBase();

    beliefBase.addBelief(C);
    beliefBase.addBelief(D);
    beliefBase.addBelief(history);

    Plan plan = new DefaultPlan(MinimizePlayGoal.class, MinimizePlayPlan.class);

    c.getPlanLibrary().addPlan(plan);

    RecieveMessages rm = new RecieveMessages();
    this.addBehaviour(rm);
  }
}