package src.Player;

import bdi4jade.belief.BeliefBase;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.Map;


public class ReplayPlan extends AbstractPlanBody {
  @Override
  public void action() {
    SendGoal sg = (SendGoal) getGoal();
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    BeliefBase bb = getBeliefBase();
    Map<String, History> history = (Map<String, History>) bb.getBelief("history").getValue();
    Map.Entry<String, History> entry = history.entrySet().iterator().next();
    String key = entry.getKey();
    History hist = entry.getValue();

    String choice = hist.getLast_selection();
    AID player = hist.getPlayer();
    Agent a = getAgent();
    history.remove(key);
    msg.setContent(choice);
    msg.setOntology("play");

    if (sg.getMessage() != null)
      msg = sg.getMessage().createReply();
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