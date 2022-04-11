
package sid.prac2;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;

public class Player extends SingleCapabilityAgent {
  private int CC, CD, DC, DD;

  protected void init() {
    Object[] args = getArguments();
    if (args.length != 4) {
      System.out.println("Wrong number of parameters for player inicialization. Arguments provided" +
              args.length + "expected four.");
      doDelete();
    }

    int CC = Integer.parseInt(args[0].toString());
    int CD = Integer.parseInt(args[1].toString());
    int DC = Integer.parseInt(args[2].toString());
    int DD = Integer.parseInt(args[3].toString());
    Belief C = new TransientBelief("C", new int[]{CC, CD});
    Belief D = new TransientBelief("D", new int[]{DC, DD});
    Belief history = new TransientBeliefSet("history", new HashSet());

    Capability cap = this.getCapability();
    BeliefBase beliefBase = cap.getBeliefBase();
    beliefBase.addBelief(C);
    beliefBase.addBelief(D);
    beliefBase.addBelief(history);
    Plan plan = new DefaultPlan(minimizePlay.class,
    minimizePlanBody.class);
    addGoal(new minimizePlay());
    getCapability().getPlanLibrary().addPlan(plan);
    //System.out.println(this.getBeliefs());
  }

  public class minimizePlay implements Goal {
    private static final String msg = "Hello World!";
    
    public String getText(){
      return msg;
    }
  }
  
  public class minimizePlanBody extends AbstractPlanBody {
  @Override
    public void action() {
    minimizePlay goal = (minimizePlay) getGoal();
    //BeliefBase bb = getBeliefBase();
    System.out.println("A minimizar");
//     int[] c = (int[]) bb.getBelief("C").getValue();
//     int[] d = (int[]) bb.getBelief("D").getValue();
//     int minc = this.min(c);
//     int mind = this.min(d);
//     if(minc <= mind) System.out.println(Arrays.toString(c));
//     else System.out.println(Arrays.toString(d));
    setEndState(Plan.EndState.SUCCESSFUL);
    }
    
    /*private int min(int[] a){
      if(a[0] < a[1]) return a[0];
      else return a[1];
    }*/
  }
}

