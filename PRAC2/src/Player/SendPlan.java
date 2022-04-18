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
    SendGoal sg = (SendGoal) getGoal();
    String choice = sg.getChoice();
    AID player = sg.getPlayer();
    Agent a = sg.getAgent();

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("alarm-management");
    template.addServices(templateSd);
    SearchConstraints sc = new SearchConstraints();
    sc.setMaxResults(Long.valueOf(10));

    try { // envia un missatge un missatge en cas que no estigui dins el rang
      DFAgentDescription[] results = DFService.search(a, template, sc);
      ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
      msg.setContent(choice);
      msg.setOntology("play");
      msg.addReceiver(player);
      System.out.println("Message sent sucessfully");
      a.send(msg);
      setEndState(Plan.EndState.SUCCESSFUL);
    } catch (Exception e) { setEndState(Plan.EndState.FAILED); }
  }
}