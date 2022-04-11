
package examples.HelloBDI;

import bdi4jade.core.*;
import bdi4jade.goal.*;
import bdi4jade.plan.*;
import bdi4jade.plan.planbody.*;

public class HelloBDI extends SingleCapabilityAgent {
  public HelloBDI(int CC, int CD, int DC y int DD) {
    Belief C = new TransientBelief("C", new int[] {CC, CD});
    Belief D = new TransientBelief("D", new int[] {DC, DD});
    Belief history = new TransientBeliefSet("history", new HashSet());

    beliefBase.addBelief(C);
    beliefBase.addBelief(D);
    beliefBase.addBelief(D);

    Plan plan = new DefaultPlan(HelloGoal.class, HelloPlanBody.class);
    Plan plan1 = new DefaultPlan(HelloGoal.class, ByePlanBody.class);

    getCapability().getPlanLibrary().addPlan(plan);
    getCapability().getPlanLibrary().addPlan(plan1);

    addGoal(new HelloGoal("world"));
  }
}