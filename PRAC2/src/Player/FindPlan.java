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
    FindGoal rg = (FindGoal) getGoal();
    Agent a = rg.getAgent();

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("player");
    template.addServices(templateSd);
    //SearchConstraints sc = new SearchConstraints();
    //sc.setMaxResults(Long.valueOf(10));
    try {
      //FALTA MIRAR QUE NO ESTES JUGANDO YA
      DFAgentDescription[] results = DFService.search(a, template);
      Set <String> s = new HashSet <>();
      for (DFAgentDescription r : results) {
        if (!r.getName().getName().equals(a.getAID().getName()))
          s.add(r.getName().getName());
      }
      Random rand = new Random();
      if (s.size() > 0) {
        int agent_game = rand.nextInt(results.length);
        AID play_against = results[agent_game].getName();
        System.out.println(play_against);
        System.out.println("sucessfully registered new game");
        setEndState(Plan.EndState.SUCCESSFUL);
        dispatchGoal(new MinimizePlayGoal(a, play_against));
      }
    } catch (Exception e) { setEndState(Plan.EndState.FAILED); }
  }
}