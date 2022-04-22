package examples.Player;

import bdi4jade.belief.BeliefBase;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Map;


public class ReplayPlan extends AbstractPlanBody {
  @Override
  public void action() {
    System.out.println("Llego a replyPlan!");
    //SendGoal sg = (SendGoal) getGoal();
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    BeliefBase bb = getBeliefBase();
    Map<String, Object> history = (Map<String, Object>) bb.getBelief("historyReplies").getValue();
    Map.Entry<String, Object> entry = history.entrySet().iterator().next();
    String key = entry.getKey();
    ArrayList<Object> hist_vect = (ArrayList<Object>) entry.getValue();
    History hist = (History) hist_vect.get(0);
    ACLMessage incoming = (ACLMessage) hist_vect.get(1);

    String choice = hist.getLast_selection();
    AID player = hist.getPlayer();
    Agent a = getAgent();
    history.remove(key);
    bb.updateBelief("historyReplies", history);
    msg.setContent(choice);
    msg.setOntology("play");

    if (incoming != null)
      msg = incoming.createReply();
    else {
      msg.addReceiver(player);
      msg.setConversationId(a.getAID().getName());
    }
    
    try {
      a.send(msg);
      setEndState(EndState.SUCCESSFUL);
    } catch (Exception e) {
      setEndState(EndState.FAILED);
    }
    
  }
}