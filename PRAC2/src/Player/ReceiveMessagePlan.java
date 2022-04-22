package examples.Player;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.goal.SequentialGoal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;
import bdi4jade.goal.*;
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

    Map<String, ArrayList<Object>> history_container = (Map<String, ArrayList<Object>>) bb.getBelief("history").getValue();


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
          History h = (History) history_container.get(name).get(0);
          if (h.getSelection() == "C") {
            if (content == "C") penalization += c[0];  //CC
            else penalization += c[1];  //CD
          } else {
            if (content == "C") penalization += d[0];  //DC
            else penalization += d[1];  //DD
          }
          h.setLastPlay(content);
          ArrayList<Object> new_hist_vect = new ArrayList<Object>();
          new_hist_vect.add(h);
          new_hist_vect.add(msg);
          history_container.replace(name, new_hist_vect);
          bb.updateBelief("history", history_container);
          bb.updateBelief("penalization", penalization);
          dispatch_sequential_minimize(content, a, msg);
        } else dispatch_sequential_choose(content, a,  msg);
      }
    }
  }
  private void dispatch_sequential_choose(String content, Agent a, ACLMessage msg){


    ArrayList<Goal> goals = new ArrayList<Goal>();
    goals.add(new ChoosePlayGoal(content, a, msg.getSender(), msg));
    goals.add(new examples.Player.ReplyGoal());
    SequentialGoal seq = new SequentialGoal(goals);
    dispatchGoal(seq);

  }
  private void dispatch_sequential_minimize(String content, Agent a, ACLMessage msg){


    ArrayList<Goal> goals = new ArrayList<Goal>();
    goals.add(new MinimizePlayGoal(a, msg.getSender(), msg));
    goals.add(new examples.Player.ReplyGoal());
    SequentialGoal seq = new SequentialGoal(goals);
    dispatchGoal(seq);

  }
}