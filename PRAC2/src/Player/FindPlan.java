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
import jade.domain.FIPAException;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;

public class FindPlan extends AbstractPlanBody {
  @Override
  public void action() {
    BeliefBase bb = getBeliefBase();
    Set<String> plays = (Set<String>) (bb.getBelief("plays").getValue());
    Set<String> started = (Set<String>) (bb.getBelief("started").getValue());

    //System.out.println ("plays find: " + plays.toString());
    FindGoal rg = (FindGoal) getGoal();
    Agent a = rg.getAgent();

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("player");
    template.addServices(templateSd);

    HashSet<AID> players = new HashSet();
    try {
      DFAgentDescription[] results = DFService.search(a, template);
      Set <String> s = new HashSet <>();
      for (DFAgentDescription r : results) {
        if (!r.getName().getName().equals(a.getAID().getName()) && !plays.contains(r.getName().getName()))
          s.add(r.getName().getName());
      }
      Random rand = new Random();
      if (s.size() > 0) {
        int agent_game = rand.nextInt(results.length);
        AID play_against = results[agent_game].getName();
        started.add(play_against.getName());
        bb.updateBelief("started", started);
        plays.add(play_against.getName());
        bb.updateBelief("plays", plays);
        System.out.println(play_against);

        System.out.println("sucessfully registered new game");
        dispatchGoal(new MinimizePlayGoal(a, play_against));
      }
    } catch (Exception e) { setEndState(Plan.EndState.FAILED); }
  }
}
