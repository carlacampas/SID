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
  private int plays;

  public SendGoal(Agent a) {
    this.a = a;
    plays = 0;
  }

  public Agent getAgent() { return a; }
  public int getPlays() { return plays; }
  public void inc_plays() {++plays;};
}