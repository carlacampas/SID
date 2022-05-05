package examples.Player;

import bdi4jade.belief.BeliefBase;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;


public class ReplayPlan extends AbstractPlanBody {
  @Override
  public void action() {
    System.out.println("Llego a replyPlan!");
    //SendGoal sg = (SendGoal) getGoal();
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    BeliefBase bb = getBeliefBase();
    LinkedList<QueueElem> queue = (LinkedList<QueueElem>) bb.getBelief("replyQueue").getValue();
    if(queue.isEmpty()) System.out.println("En ReplyPlan history está vacio");
    /*Map.Entry<String, Object> entry = history.entrySet().iterator().next();
    String key = entry.getKey();
    ArrayList<Object> hist_vect = (ArrayList<Object>) entry.getValue();*/
    QueueElem qelem = queue.poll();
    History hist = qelem.getHistory();
    ACLMessage incoming = qelem.getMessage();

    String choice = hist.getSelection();
    AID player = hist.getPlayer();
    Agent a = getAgent();

    bb.updateBelief("replyQueue", queue);
    msg.setContent(choice);
    msg.setOntology("play");

    if (incoming != null)
      msg = incoming.createReply();
    else {
      msg.addReceiver(player);
      msg.setConversationId(a.getAID().getName());
    }
    
    try {
      msg.setContent(choice);
      System.out.println("Contenido antes de reply: " + msg.getContent());
      a.send(msg);
      setEndState(EndState.SUCCESSFUL);
    } catch (Exception e) {
      setEndState(EndState.FAILED);
    }
    
  }
}