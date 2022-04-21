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
    Map<String, AID> found_agents = (Map<String, AID>) (bb.getBelief("found_agents").getValue());

    FindGoal rg = (FindGoal) getGoal();
    Agent a = rg.getAgent();

    if (found_agents.size() == 0) {
      DFAgentDescription template = new DFAgentDescription();
      ServiceDescription templateSd = new ServiceDescription();
      templateSd.setType("player");
      template.addServices(templateSd);
      //SearchConstraints sc = new SearchConstraints();
      //sc.setMaxResults(Long.valueOf(10));
      try {
        DFAgentDescription[] results = DFService.search(a, template);
        for (DFAgentDescription r : results) {
          if (!r.getName().getName().equals(a.getAID().getName()) && !plays.contains(r.getName().getName()))
            found_agents.put(r.getName().getName(), r.getName());
        }
      } catch (Exception e) { setEndState(Plan.EndState.FAILED); }
    }

    if (found_agents.size() > 0) {
      AID[] results = found_agents.values().toArray(new AID[found_agents.size()]);
      Random rand = new Random();
      int agent_game = rand.nextInt(results.length);
      AID play_against = results[agent_game];
      plays.add(play_against.getName());
      found_agents.remove(play_against.getName());
      bb.updateBelief("plays", plays);
      bb.updateBelief("found_agents", found_agents);
      System.out.println("sucessfully registered new game");
      dispatchGoal(new MinimizePlayGoal(a, play_against, null));
    }
  }
}