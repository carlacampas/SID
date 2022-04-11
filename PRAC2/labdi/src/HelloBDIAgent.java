package sid.bdiexamples;

import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;





public class HelloBDIAgent extends SingleCapabilityAgent {
  
  public class HelloGoal implements Goal {
    
    private static final String msg = "Hello World!";
    
    public String getText(){
    
      return msg;
    
    }

  }
  
  public class HelloPlanBody extends AbstractPlanBody {
  @Override
    public void action() {
    HelloGoal goal = (HelloGoal) getGoal();
    
    System.out.println("Hello world: 0");
    System.out.println("Hello world: 1");
    System.out.println("Hello world: 2");
    System.out.println("Hello world: 3");
    System.out.println("Hello world: 4");
    System.out.println("Hello world: 5");
    System.out.println("Hello world: 6");
    System.out.println("Hello world: 7");
    System.out.println("Hello world: 8");
    System.out.println("Hello world: 9");
    setEndState(Plan.EndState.SUCCESSFUL);
    }
  }
  
  public class HelloPlanBody2 extends AbstractPlanBody {
  @Override
    public void action() {
    HelloGoal goal = (HelloGoal) getGoal();
    
    System.out.println("Bye world: 0");
    System.out.println("Bye world: 1");
    System.out.println("Bye world: 2");
    System.out.println("Bye world: 3");
    System.out.println("Bye world: 4");
    System.out.println("Bye world: 5");
    System.out.println("Bye world: 6");
    System.out.println("Bye world: 7");
    System.out.println("Bye world: 8");
    System.out.println("Bye world: 9");
    setEndState(Plan.EndState.FAILED);
    }
  }

  public HelloBDIAgent() {
    Plan plan = new DefaultPlan(HelloGoal.class,
    HelloPlanBody.class);
    Plan plan2 = new DefaultPlan(HelloGoal.class,
    HelloPlanBody2.class);
    addGoal(new HelloGoal());
    //addGoal(new HelloGoal());
    getCapability().getPlanLibrary().addPlan(plan);
    getCapability().getPlanLibrary().addPlan(plan2);
  }
  
}



