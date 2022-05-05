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

    LinkedList<QueueElem> queue = (LinkedList<QueueElem>) bb.getBelief("sendQueue").getValue();
    if(!queue.isEmpty()) System.out.println("Hay cosas que enviar!");

    QueueElem qelem = queue.poll();
    History hist = qelem.getHistory();
    ACLMessage incoming = qelem.getMessage();

    String choice = hist.getSelection();
    //System.out.println("choice es: " + choice);
    AID player = hist.getPlayer();
    //System.out.println("AID de player es: " + player.getName());
    Agent a = getAgent();
    bb.updateBelief("sendQueue", queue);

    if (incoming != null)
      msg = incoming.createReply();
    else {
      msg.setConversationId(a.getAID().getName());
    }

    msg.setContent(choice);
    msg.setOntology("play");
    msg.addReceiver(player);
    //System.out.println("Intentando imprimir contenido: " + msg.getContent());
    try {
      System.out.println("contenido antes de enviar en Send: " + msg.getContent());

      a.send(msg);
      setEndState(Plan.EndState.SUCCESSFUL);
    } catch (Exception e) {
      setEndState(Plan.EndState.FAILED);
    }


  }
}