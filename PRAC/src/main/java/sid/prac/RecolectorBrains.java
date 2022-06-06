package sid.prac;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import bdi4jade.core.*;

import java.util.List;

import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.env.Observation;
import java.util.Random;
import java.util.ArrayList;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class RecolectorBrains extends SingleCapabilityAgent {
	
	private static final long serialVersionUID = 1L;
	AID body;
	Observation collectionType;
	List <Couple<String, List <Couple<Observation, Integer>>>> ob;
	
	
	protected void init() {
		System.out.println("Brains se despierta!");
		Object[] args = getArguments();
		if (args.length != 3) {
		      System.out.println("incorrect args");
		      doDelete();
		}
		
		body = (AID) args[0];
		collectionType = (Observation) args[1];
		ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) args[2];
		
		System.out.println("Brain waking up, successfully linked to body agent: " + body.getName());
		
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		Belief observations = new TransientBelief("observations", ob);
		bb.addBelief(observations);
		
		Plan getGold = new DefaultPlan(GetTreasureGoal.class, GetTreasurePlan.class);
		
		c.getPlanLibrary().addPlan(getGold);
		this.addGoal(new GetTreasureGoal());
		addBehaviour(new RecieveObservations());
	}
	
	// GET GOLD
	public class GetTreasureGoal implements Goal {
		List <Couple<String, List <Couple<Observation, Integer>>>> ob;
		
		public void setObservations(List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
			this.ob = ob;
		}
		
		public String getNextPosition() {
			String next = "";
			Integer best = 0;
			List <String> possibleMoves = new ArrayList<>();
			for(Couple<String, List<Couple<Observation, Integer>>> o : ob) {
				Integer q = 0;
				boolean possible = true;
				for (Couple<Observation, Integer> elem : o.getRight()) {
					if (elem.getLeft() == collectionType) q += elem.getRight();
					else if (elem.getLeft() == Observation.WIND) possible = false;
				}
				if (q > best) next = o.getLeft();
				else if (possible) possibleMoves.add(o.getLeft());
			}
			if (best == 0 && possibleMoves.size() > 0) {
				Random rand = new Random();
				int pos = rand.nextInt(possibleMoves.size());
				next = possibleMoves.get(pos);
			}
			else if (best == 0) {
				Random rand = new Random();
				int pos = rand.nextInt(ob.size());
				next = ob.get(pos).getLeft();
			}
			return next;
		}
	}
	
	public class GetTreasurePlan extends AbstractPlanBody {
		
		@Override
		public void action() {
			BeliefBase bb = getBeliefBase();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
			System.out.println(ob.toString());
			GetTreasureGoal tg = (GetTreasureGoal) getGoal();
			
			tg.setObservations(ob);
			String nextMove = tg.getNextPosition();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(body);
			msg.setSender(getAID());
            msg.setContent(nextMove);
            send(msg);
            System.out.println("NEXT MOVE: " + nextMove);
            setEndState(Plan.EndState.SUCCESSFUL);
		}
	}
	
	public class RecieveObservations extends CyclicBehaviour {
		private MessageTemplate tpl;
		private ACLMessage msg;
		
        public void onStart() {
        	MessageTemplate tpl1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			MessageTemplate tpl2 = MessageTemplate.MatchSender(body);
			tpl = MessageTemplate.and(tpl1, tpl2);
        }
        
        public void action() {
            msg = myAgent.receive(tpl);

            if (msg != null) {
                try {
					List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) msg.getContentObject();
					
					Capability c = getCapability();
					BeliefBase bb = c.getBeliefBase();
					
					Belief observations = new TransientBelief("observations", ob);
					bb.addOrUpdateBelief(observations);
					addGoal(new GetTreasureGoal());
				} catch (UnreadableException e) { e.printStackTrace(); }
            }
            else { block(); }
        }
	}
}
