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
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.wrapper.AgentContainer;
import jade.domain.FIPAException;
import jade.domain.DFService;

import sid.prac.ExplorerBrains;

public class GeneralAgent extends AbstractDedaleAgent {
	private MapRepresentation map;
	private String type;
	private AID brains;
	
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
				
				if (sum == 0) {
					type = "explorer";
					
					myAgent.addBehaviour(new ExploSoloBehaviour((AbstractDedaleAgent) this.myAgent, map));
					addExplorerBrains();
					System.out.println("I am an explorer!");
				}
				else {
					type = "recolector";
					myAgent.addBehaviour(new RandomWalkBehaviour((AbstractDedaleAgent) this.myAgent));
					addRecolectorBrains();
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
	
	public void addExplorerBrains() {
		//get the parameters given when creating the agent into the object[]
		final Object[] args = getArguments();
		//use them as parameters for your behaviours 
		System.out.println("Llego a setup de Explorer");
		AgentContainer ac = getContainerController();
		try {
			AgentController ag = ac.createNewAgent("brainy_" + getName(), "sid.prac.ExplorerBrains", new Object[]{getAID()});
			ag.start();
			brains = new AID(ag.getName(), AID.ISLOCALNAME);
			
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addRecolectorBrains() {
		//get the parameters given when creating the agent into the object[]
		final Object[] args = getArguments();
		//use them as parameters for your behaviours 
		System.out.println("Llego a setup de Recolector");
		AgentContainer ac = getContainerController();
		try {
			AgentController ag = ac.createNewAgent("brainy_" + getName(), "sid.prac.RecolectorBrains", new Object[]{getAID()});
			ag.start();
			brains = new AID(ag.getName(), AID.ISLOCALNAME);
			
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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