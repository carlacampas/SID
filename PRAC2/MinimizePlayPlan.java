package examples.HelloBDI;

import bdi4jade.core.*;
import bdi4jade.goal.*;
import bdi4jade.plan.*;
import bdi4jade.plan.planbody.*;

public class MinimizePlayPlan extends AbstractPlanBody {
  int count = 0;
  @Override
  public void action() {
    setEndState(Plan.EndState.FAILED);
    HelloGoal goal = (HelloGoal) getGoal();
    System.out.println("Bye " + goal.getText() + ": " + String.valueOf(count));
    count++;
    if (count == 10) setEndState(Plan.EndState.SUCCESSFUL);
  }
}