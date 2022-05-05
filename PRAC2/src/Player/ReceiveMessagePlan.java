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

    Map<String, History> history_container = (Map<String, History>) bb.getBelief("history").getValue();



    int penalization = (int) bb.getBelief("penalization").getValue();
    int[] c = (int[]) (bb.getBelief("C").getValue());
    int[] d = (int[]) (bb.getBelief("D").getValue());

    ReceiveMessageGoal rg = (ReceiveMessageGoal) getGoal();
    Agent a = rg.getAgent();

    MessageTemplate tpl = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchOntology("play"));
    // Cyclic behaviour
    //System.out.println("Antes de comprobar si mensaje nulo");
    ACLMessage msg = a.receive(tpl);
    if (msg != null) {
      //System.out.println("Recibo mensaje no nulo!");

      String content = msg.getContent(), name = msg.getSender().getName();
      if (content != null && (content.equals("C") || content.equals("D"))) {

        //System.out.println("Contenido no nulo y correcto!");
        if (msg.getConversationId().equals(a.getAID().getName())) {
          if(history_container.isEmpty()) System.out.println("History container vacio en Receive");
          if(history_container.get(name) == null) System.out.println(
                  "Soy agente " + getAgent().getName() + " " + name +" no está en el map!");
          History h = history_container.get(name);
          if (h.getSelection() == "C") {
            if (content == "C") penalization += c[0];  //CC
            else penalization += c[1];  //CD
          } else {
            if (content == "C") penalization += d[0];  //DC
            else penalization += d[1];  //DD
          }
          h.setLastPlay(content);
          history_container.put(name, h);
          bb.updateBelief("history", history_container);
          bb.updateBelief("penalization", penalization);
          dispatch_sequential_minimize(content, a, msg);
        } else{

          dispatch_sequential_choose(content, a,  msg);

        }
      }
    }

  }
  private void dispatch_sequential_choose(String content, Agent a, ACLMessage msg){


    ArrayList<Goal> goals = new ArrayList<Goal>();
    goals.add(new ChoosePlayGoal(content, a, msg.getSender(), msg));
    goals.add(new ReplyGoal());
    SequentialGoal seq = new SequentialGoal(goals);
    dispatchGoal(seq);

  }
  private void dispatch_sequential_minimize(String content, Agent a, ACLMessage msg){


    ArrayList<Goal> goals = new ArrayList<Goal>();
    goals.add(new MinimizePlayGoal(a, msg.getSender(), msg));
    goals.add(new SendGoal());
    SequentialGoal seq = new SequentialGoal(goals);
    dispatchGoal(seq);

  }

  private void print_history_container(Map<String, History> m) {
    for (Map.Entry<String, History> e : m.entrySet()) System.out.println(e.getValue().getPlayer());
  }
}

