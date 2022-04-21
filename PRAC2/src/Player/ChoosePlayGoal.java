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
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class ChoosePlayGoal implements Goal {
  private Agent my_agent;
  private String choice;
  private AID against;
  private ACLMessage msg;

  public ChoosePlayGoal(String choice, Agent a, AID against, ACLMessage msg) {

    my_agent = a;
    this.choice = choice;
    this.against = against;
    this.msg = msg;

  }

  public Agent getAgent() { return my_agent; }
  public String getChoice() { return choice; }
  public AID getAgainst() { return against; }
  public ACLMessage getMessage() { return msg; }
}