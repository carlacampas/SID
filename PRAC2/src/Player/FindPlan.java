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
    System.out.println("Find Plan:");
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
          if (!r.getName().getName().equals(a.getAID().getName())) players.add(r.getName());
        }
      } catch (Exception e) {}
      this.getBeliefBase().updateBelief("games", players);
      dispatchGoal(new SendGoal(a));
      setEndState(Plan.EndState.SUCCESSFUL);
    System.out.println(this.getBeliefBase().getBelief("games"));
    System.out.println("-------------------------------------------------");
  }
}