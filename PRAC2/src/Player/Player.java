
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

  protected void init() {
    Object[] args = getArguments();
    if (args.length != 4) {
      System.out.println("incorrect args");
      doDelete();
    }

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("player");
    template.addServices(templateSd);
    //SearchConstraints sc = new SearchConstraints();
    //sc.setMaxResults(Long.valueOf(10));
    /*
    try { // envia un missatge un missatge en cas que no estigui dins el rang
      DFAgentDescription[] results = DFService.search(this, template, null);
      Random rand = new Random();
      int agent_game = rand.nextInt(results.length);
      DFAgentDescription
      ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
      msg.setContent("new game");
      int count = 0;
      for (DFAgentDescription r : results) {
        String a = getAID().getName();
        String b = r.getName().getName();
        if (!a.equals(b)) {
          msg.addReceiver(r.getName());
          count++;
        }
      }
      if (count > 0) send(msg);
    } catch (Exception e) {} */

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

    c.getPlanLibrary().addPlan(reg);
    this.addGoal(new RegisterGoal(this));
  }
}