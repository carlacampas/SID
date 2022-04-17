package examples.Player;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;

public class MinimizePlayGoal implements Goal {
  int val;
  public MinimizePlayGoal() {
    val = 0;
  }

  public int getVal() {
    return val;
  }
}