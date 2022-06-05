package sid.prac;

import jade.core.*;
import bdi4jade.core.*;
import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

public class RecolectorBrains extends SingleCapabilityAgent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AID body;
	private MapRepresentation myMap;
	
	
	protected void init() {
		System.out.println("Brains se despierta!");
		Object[] args = getArguments();
		if (args.length != 1) {
		      System.out.println("incorrect args");
		      doDelete();
		}
		body = (AID) args[0];
		System.out.println("Brain waking up, successfully linked to body agent: " + body.getName());
		
		
	}
}
