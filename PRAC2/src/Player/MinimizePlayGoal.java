package examples.Player;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;
import jade.core.Agent;
import jade.core.AID;

public class MinimizePlayGoal implements Goal {
  private Agent a;
  private AID player;

  public MinimizePlayGoal(Agent a, AID player) {
    this.a = a;
    this.player = player;
  }
}