
package sid.prac2;

import java.util.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
//import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.planbody.*;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;
import bdi4jade.goal.*;

public class PlayerV3 extends SingleCapabilityAgent {
  private int CC, CD, DC, DD;
  
  protected void init() {
    /*
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());

    ServiceDescription sd = new ServiceDescription();
    sd.setType("player");
    sd.setName(getName());

    DFAgentDescription template = // fill the template
            Behaviour b = new SubscriptionInitiator(
            this,
            DFService.createSubscriptionMessage(this, dfd, template, null))
    {
      protected void handleInform(ACLMessage inform) {
        try {
          DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
          // do something
        }
        catch (FIPAException fe) {
          fe.printStackTrace();
        }
      }
    };
    addBehaviour(b);
     */
    
    Object[] args = getArguments();
    if (args.length != 4) {
      System.out.println("Wrong number of parameters for player inicialization. Arguments provided" +
              args.length + "expected four.");
      doDelete();
    }
    System.out.println("Ejecutando init.");
    int CC = Integer.parseInt(args[0].toString());
    int CD = Integer.parseInt(args[1].toString());
    int DC = Integer.parseInt(args[2].toString());
    int DD = Integer.parseInt(args[3].toString());
    Belief C = new TransientBelief("C", new ArrayList<Integer>( Arrays.asList(new Integer[]{CC, CD})));
    Belief D = new TransientBelief("D", new ArrayList<Integer>(Arrays.asList(new Integer[]{DC, DD})));
    Belief history = new TransientBeliefSet("history", new HashSet());

    Capability cap = this.getCapability();
    BeliefBase beliefBase = cap.getBeliefBase();
    beliefBase.addBelief(C);
    beliefBase.addBelief(D);
    beliefBase.addBelief(history);
    ArrayList<Integer> Cv = new ArrayList<Integer>(Arrays.asList(new Integer[]{CC, CD}));
    ArrayList<Integer> Dv = new ArrayList<Integer>(Arrays.asList(new Integer[]{DC, DD}));
    
    ArrayList<Goal> goals = new ArrayList<Goal>();
    
    goals.add(new BeliefPresentGoal("jugada"));
    goals.add(new PredicateGoal("realizada", true));
    SequentialGoal sequentialGoal = new SequentialGoal(goals);
    this.addGoal(sequentialGoal);
    
    GoalTemplate goalTemplateJugada = GoalTemplateFactory.hasBelief("jugada");
    GoalTemplate goalTemplateRealizada = GoalTemplateFactory.hasBeliefValueOfType("realizada", Boolean.class);
    
    
    Plan planJugada = new DefaultPlan(goalTemplateJugada,
    elegirPlan.class);
    Plan planRealizada = new DefaultPlan(goalTemplateRealizada, realizarPlan.class);
    getCapability().getPlanLibrary().addPlan(planJugada);
    getCapability().getPlanLibrary().addPlan(planRealizada);
    
    System.out.println("Termino init.");
    
  }
  
  public class elegirPlan extends BeliefGoalPlanBody {
    @Override
    protected void execute() {
    
      BeliefBase bb = getBeliefBase();
      TransientBelief jugada = new TransientBelief("jugada", "Saludar");
      bb.addOrUpdateBelief(jugada);
    
    }
    
  
  }
  
  public class realizarPlan extends BeliefGoalPlanBody {
    @Override
    protected void execute() {
      BeliefBase bb = getBeliefBase();
      TransientBelief jugada = (TransientBelief) bb.getBelief("jugada");
      String sjug = (String) jugada.getValue(); 
      System.out.println("La jugada es: " + sjug);
      bb.addOrUpdateBelief(new TransientPredicate("realizada", true));
      //bb.removeBelief("jugada");
    
    }
  
  
  }
  
}



