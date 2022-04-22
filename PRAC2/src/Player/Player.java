
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
    Belief history = new TransientBelief("history", new HashMap());
    Belief history2 = new TransientBelief("historyReplies", new HashMap());
    Belief plays = new TransientBeliefSet("plays", new HashSet());
    Belief found_agents = new TransientBelief("found_agents", new HashMap());
    Belief penalization = new TransientBelief("penalization", 0);

    Capability c = getCapability();
    BeliefBase bb = c.getBeliefBase();

    bb.addBelief(C);
    bb.addBelief(D);
    bb.addBelief(history);
    bb.addBelief(history2);
    bb.addBelief(plays);
    bb.addBelief(found_agents);
    bb.addBelief(penalization);

    Plan reg = new DefaultPlan(RegisterGoal.class, RegisterPlan.class);
    Plan receive_message = new DefaultPlan(ReceiveMessageGoal.class, ReceiveMessagePlan.class);
    Plan find_agents = new DefaultPlan(FindGoal.class, FindPlan.class);
    Plan play = new DefaultPlan(MinimizePlayGoal.class, MinimizePlayPlan.class);
    Plan send = new DefaultPlan(SendGoal.class, SendPlan.class);
    Plan choose = new DefaultPlan(ChoosePlayGoal.class, ChoosePlayPlan.class);
    Plan reply_message = new DefaultPlan(ReplyGoal.class, ReplayPlan.class);


    c.getPlanLibrary().addPlan(reg);
    c.getPlanLibrary().addPlan(receive_message);
    c.getPlanLibrary().addPlan(find_agents);
    c.getPlanLibrary().addPlan(play);
    c.getPlanLibrary().addPlan(send);
    c.getPlanLibrary().addPlan(choose);
    c.getPlanLibrary().addPlan(reply_message);

    this.addGoal(new RegisterGoal(this));
  }
}