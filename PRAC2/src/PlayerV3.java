
package sid.prac2;

import java.util.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;
import jade.proto.SubscriptionInitiator;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
//import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.planbody.*;
import bdi4jade.plan.Plan;
import bdi4jade.belief.*;
import bdi4jade.core.*;
import bdi4jade.goal.*;

public class PlayerV3 extends SingleCapabilityAgent { // Una capability

  // Penalizaciones
  private int CC, CD, DC, DD;

  // Agentes disponibles para jugar
  Set <AID> avaliable_agents = new HashSet <AID>();

  // Penalización acumulada
  private int penalizacion = 0;

  private Logger myLogger = Logger.getMyLogger(getClass().getName());

  /*
  public class BuscarAgents extends TickerBehaviour
  {
        public BuscarAgents(Agent a, long timeout)
        {
            super(a,timeout);
        }

        // Ticker behaviour
        public void onTick() {

          /*  
          // Busqueda otros agentes
          DFAgentDescription template = new DFAgentDescription();
          ServiceDescription templateSd = new ServiceDescription();
          templateSd.setType("player");
          template.addServices(templateSd);
          SearchConstraints sc = new SearchConstraints();
          sc.setMaxResults(Long.valueOf(10));

          avaliable_agents.clear();

          try {
              DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);

              for(int i = 0; i<results.length; ++i){
                  DFAgentDescription dfd2 = results[i];
                  AID p = dfd2.getName();
                  if(p!=getAID()) avaliable_agents.add(p);
              }
            } catch (Exception e) {}
          
            
          //System.out.print("Agent: " + getName() + " ");
          //System.out.println(avaliable_agents);
    
        }

  }*/

  protected void init() {

    Object[] args = getArguments(); 
    if (args.length != 4) {   // Numero de parametros de entrada incorrecto
      System.out.println("Wrong number of parameters for player inicialization. Arguments provided" +
              args.length + "expected four.");
      doDelete();
    }

    // Penalizaciones recibidas como parametros de entrada
    int CC = Integer.parseInt(args[0].toString());
    int CD = Integer.parseInt(args[1].toString());
    int DC = Integer.parseInt(args[2].toString());
    int DD = Integer.parseInt(args[3].toString());

    // Creación de beliefs
    Belief C = new TransientBelief("C", new ArrayList<Integer>( Arrays.asList(new Integer[]{CC, CD})));
    Belief D = new TransientBelief("D", new ArrayList<Integer>(Arrays.asList(new Integer[]{DC, DD})));


    // Registro al DF
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("player");
    sd.setName(getName());
    dfd.setName(getAID());
    dfd.addServices(sd);

    try {
        DFService.register(this,dfd);
        System.out.println("player: " + getName() + " registered.");
    } catch (FIPAException e) {
        myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
        doDelete();
    }

    //DFAgentDescription template = // fill the template
    Behaviour b = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, dfd, template, null))
    {
      protected void handleInform(ACLMessage inform) {
        try {
          DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
          for(int i = 0; i<dfds.length; ++i){
                  System.out.println(dfds[i]);
              }
            } catch (Exception e) {}// do something
        }
    };
    addBehaviour(b);

    float ms = 1000;
    // Añadimos el ticker behaviour
    //BuscarAgents t = new BuscarAgents(this, Math.round(ms));
    //this.addBehaviour(t);

    //-------------------------------------------------------------------------------
  
/*
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
    
    System.out.println("Termino init.");*/
    
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

  // Desfegistre del DF al takeDown (quan es mor l'agent)
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("player: " + getName() + " desregistered.");
        } catch (FIPAException e) {}
    }
  
}



