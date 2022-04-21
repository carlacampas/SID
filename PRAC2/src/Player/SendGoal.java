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

public class SendGoal implements Goal {
  private Agent a;
  private AID player;
  private String choice;

  public SendGoal(Agent a, AID player, String choice) {
    this.a = a;
    this.player = player;
    this.choice = choice;
  }

  public Agent getAgent() { return a; }
  public AID getPlayer() { return player; }
  public String getChoice() { return choice; }
}