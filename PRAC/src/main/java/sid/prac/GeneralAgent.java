package sid.prac;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.knowledge.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.DFService;

public class GeneralAgent extends AbstractDedaleAgent {
	private MapRepresentation map;
	
	public MapRepresentation getMap() { return map; }
	public void setup() {
		super.setup();
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		lb.add(new OneShotBehaviour() {
			public void action () {
				List<Couple<Observation, Integer>> free = getBackPackFreeSpace();
				Iterator<Couple<Observation, Integer>> iter=free.iterator();
				int sum = 0;
				while (iter.hasNext()) {
					sum += iter.next().getRight();
				}
				
				String type;
				if (sum == 0) {
					type = "explorer";
					
					myAgent.addBehaviour(new ExploSoloBehaviour((AbstractDedaleAgent) this.myAgent, map));
					System.out.println("I am an explorer!");
				}
				else {
					type = "recolector";
					myAgent.addBehaviour(new RandomWalkBehaviour((AbstractDedaleAgent) this.myAgent));
					System.out.println("I am a collector!");
				}
				
				 // Registre al DF
		        DFAgentDescription dfd = new DFAgentDescription();
		        ServiceDescription sd = new ServiceDescription();
		        sd.setType(type);
		        sd.setName(getName());
		        dfd.setName(getAID());
		        dfd.addServices(sd);

		        try {
		            DFService.register(this.myAgent,dfd);
		        } catch (FIPAException e) {
		            doDelete();
		        }
			}
		});
		
		addBehaviour(new startMyBehaviours(this,lb));
		System.out.println("here, done with behaviour");
	}
	
	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		try {
			 DFService.deregister(this);
	     } catch (FIPAException e) {}
		super.takeDown();
	}
	
	/**
	 * This method is automatically called before migration. 
	 * You can add here all the saving you need
	 */
	protected void beforeMove(){
		super.beforeMove();
	}
	
	/**
	 * This method is automatically called after migration to reload. 
	 * You can add here all the info regarding the state you want your agent to restart from 
	 * 
	 */
	protected void afterMove(){
		super.afterMove();
	}
}
