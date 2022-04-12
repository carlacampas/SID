
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
    BeliefSetHasValueGoal setb = (BeliefSetHasValueGoal) this.getGoal();
    minimizePlay goal = (minimizePlay) getGoal();
    BeliefBase bb = getBeliefBase();
    System.out.println("A minimizar");
    int[] c = (int[]) bb.getBelief("C").getValue();
    int[] d = (int[]) bb.getBelief("D").getValue();
    int minc = this.min(c);
    int mind = this.min(d);
    TransientBeliefSet hist = (TransientBeliefSet) bb.getBelief("history");
    if(minc <= mind){
      System.out.println(Arrays.toString(c));
      hist.addValue(c);
    }
    else{
      System.out.println(Arrays.toString(d));
      hist.addValue(d);
      
    }
    System.out.println("history es: ");
    HashSet s = (HashSet) hist.getValue();
    printHashSet(s);
    setEndState(Plan.EndState.SUCCESSFUL);
    }
    
    private int min(int[] a){
      if(a[0] < a[1]) return a[0];
      else return a[1];
    }
    
    private void printHashSet(HashSet s){
      for (Object v: s){
        
        int[] elem = (int[]) v; 
        System.out.println(Arrays.toString(elem));
      }
    }
    
  }
  public class notEmptyPlan extends BeliefGoalPlanBody {
    @Override
    protected void execute() {
      
      System.out.println("A minimizar");
      BeliefBase bb = getBeliefBase();
      ArrayList<Integer> c = (ArrayList<Integer>) bb.getBelief("C").getValue();
      ArrayList<Integer> d = (ArrayList<Integer>) bb.getBelief("D").getValue();
      int minc = this.min(c);
      int mind = this.min(d);
      TransientBeliefSet hist = (TransientBeliefSet) bb.getBelief("history");
      if(minc <= mind){
        System.out.println(c);
        hist.addValue(c);
      }
      else{
        System.out.println(d);
        hist.addValue(d);
      }
      
      //bb.addOrUpdateBelief(new TransientPredicate("notEmpty", true));
      System.out.println("Termino notEmptyPlan.");
    }
      
    private int min(ArrayList<Integer> a){
      if(a.get(0) < a.get(1)) return a.get(0);
      else return a.get(1);
    }
      
    
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



