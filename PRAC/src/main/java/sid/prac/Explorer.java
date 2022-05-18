package sid.prac;

import java.util.*;

import bdi4jade.core.SingleCapabilityAgent;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.wrapper.StaleProxyException;
import sid.prac.*;

public class Explorer extends AbstractDedaleAgent{
	
	private static final long serialVersionUID = -2991562876411096907L;
	private MapRepresentation myMap;
	private AID brains;

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){
		super.setup();

		//get the parameters given when creating the agent into the object[]
		final Object[] args = getArguments();
		//use them as parameters for your behaviours 
		System.out.println("Llego a setup de Explorer");
		sid.prac.ExplorerBrains br = new sid.prac.ExplorerBrains();
		jade.wrapper.AgentContainer ag = getContainerController();
		try {
			ag.createNewAgent("brainy", "sid.prac.ExplorerBrains", new Object[]{getAID()});
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Agente cerebro creado correctamente!");
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of you agent here
		 * 
		 ************************************************/
		
		lb.add(new ExplorerBehaviour(this, myMap));
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT(S) TO BE DEPLOYED CORRECTLY WITH DEDALE
		 */
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("Starting my custom explorer agent with AID: " + this.getAID().getName());

	}


	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
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