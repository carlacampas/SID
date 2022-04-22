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
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class SendPlan extends AbstractPlanBody {
  @Override
  public void action() {

    BeliefBase bb = getBeliefBase();
    int penalization = (int) bb.getBelief("penalization").getValue();
    //System.out.println(sg.getAgent().getAID().getName() + " " + penalization);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

    Map<String, Object> history = (Map<String, Object>) bb.getBelief("history").getValue();
    try {
      Map.Entry<String, Object> entry = history.entrySet().iterator().next();

      String key = entry.getKey();
      ArrayList<Object> hist_vect = (ArrayList<Object>) entry.getValue();
      History hist = (History) hist_vect.get(0);
      ACLMessage incoming = (ACLMessage) hist_vect.get(1);

      String choice = hist.getLast_selection();
      AID player = hist.getPlayer();
      Agent a = getAgent();
      history.remove(key);
      bb.updateBelief("history", history);
      msg.setContent(choice);
      msg.setOntology("play");
      Map.Entry<String, Object> entry = history.entrySet().iterator().next();

      if (incoming != null)
        msg = incoming.createReply();
      else {
        msg.setConversationId(a.getAID().getName());
      }

      msg.setContent(choice);
      msg.setOntology("play");
      msg.addReceiver(player);

      try {
        a.send(msg);
        setEndState(Plan.EndState.SUCCESSFUL);
      } catch (Exception e) {
        setEndState(Plan.EndState.FAILED);
      }
    }
    catch (Exception e) {

      System.out.println("Esta vacio");

    }
  }
}