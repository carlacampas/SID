package sid.prac;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sid.prac.RecolectorBrains.GetTreasureGoal;
import sid.prac.RecolectorBrains.GetTreasurePlan;
import sid.prac.RecolectorBrains.RecieveObservations;
import bdi4jade.core.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
//import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;


import java.sql.Timestamp;

public class ExplorerBrains extends SingleCapabilityAgent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	AID body;
	Observation collectionType;
	
	public boolean hasGold (Couple <Long, HashMap<Observation, Integer>> obs) {
		return !(obs == null) && obs.getRight().containsKey(Observation.GOLD);
	}
	
	public HashMap<Observation, Integer> obsHashMap(List<Couple<Observation, Integer>> l) {
		HashMap<Observation, Integer> ret = new HashMap<>();
		for (Couple<Observation, Integer> o : l) {
			ret.put(o.getLeft(), o.getRight());
		}
		return ret;
	}
	
	// Crear nuevos nodos
	public void setNewNodesOntology () {
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		//System.out.println(bb.toString());
		String current = (String) (bb.getBelief("currentPosition").getValue());
		MapRepresentation map = (MapRepresentation) (bb.getBelief("map").getValue());
		HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) (bb.getBelief("mapping").getValue());
		List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
		

		map.addNewNode(current);
		
		// add all new nodes
		for (Couple <String, List<Couple<Observation, Integer>>> o : ob) {
			boolean open = true;
			boolean agent = false;
			
			for (Couple<Observation, Integer> obs : o.getRight()) {
				if (obs.getLeft() == Observation.AGENTNAME) {
					agent = true;
				}
				else if (obs.getLeft() == Observation.LOCKPICKING && obs.getRight() == 0) open = false;
			}
			
			if (!o.getLeft().equals(current)) {
				if (agent) map.addNode(o.getLeft(), MapAttribute.agent);
				else if (!open) map.addNode(o.getLeft(), MapAttribute.closed);
				else map.addNode(o.getLeft(), MapAttribute.open);
				map.addEdge(current, o.getLeft());
				
				if (!hasGold(mapping.get(o.getLeft()))) 
					mapping.put(o.getLeft(), new Couple(System.nanoTime(), obsHashMap(o.getRight())));
			}
			else mapping.put(o.getLeft(), new Couple(System.nanoTime(), obsHashMap(o.getRight())));
		}
		
		Belief map_updated = new TransientBelief("map", map);
		Belief mapping_updated = new TransientBelief("mapping", mapping);
		bb.addOrUpdateBelief(map_updated);
		bb.addOrUpdateBelief(mapping_updated);
	}	
	
	protected void init() {
		System.out.println("ExplorerBrains se despierta!");
		Object[] args = getArguments();
		if (args.length != 4) {
		      System.out.println("incorrect args");
		      doDelete();
		}
		
		body = (AID) args[0];
		collectionType = (Observation) args[1];
		String currentPosition = (String) args[2];
		List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) args[3];		
		
		System.out.println("Brain waking up, successfully linked to body agent: " + body.getName());
		
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		Belief observations = new TransientBelief("observations", ob);
		Belief currentNode = new TransientBelief("currentPosition", currentPosition);
		HashMap tmp = new HashMap<String, Integer>();
		Belief visitedNodes = new TransientBelief("visitedNodes", tmp);
		Belief sum = new TransientBelief("suma", (Double) 0.0);
		Belief K = new TransientBelief("K", (Double) 0.0);
		Belief Ex = new TransientBelief("Ex", (Double) 0.0);
		Belief Ex2 = new TransientBelief("Ex2", (Double) 0.0);
		Belief mapping = new TransientBelief("mapping", new HashMap<String, Couple <Long, HashMap<Observation, Integer>>>());
		Belief map = new TransientBelief("map", new MapRepresentation());
		tmp = new HashMap<String, Boolean>();
		Belief Agents = new TransientBelief("Agents",tmp);
		bb.addBelief(observations);
		bb.addBelief(currentNode);
		bb.addBelief(visitedNodes);
		bb.addBelief(sum);
		bb.addBelief(K);
		bb.addBelief(Ex);
		bb.addBelief(Ex2);
		bb.addBelief(Agents);
		bb.addBelief(mapping);
		bb.addBelief(map);
		
		setNewNodesOntology();
		
		Plan getObjective = new DefaultPlan(GetObjectiveGoal.class, GetObjectivePlan.class);
		//Plan informAgent = new DefaultPlan(InformAgentGoal.class, InformAgentPlan.class);
		//c.getPlanLibrary().addPlan(informAgent);
		c.getPlanLibrary().addPlan(getObjective);
		
		this.addGoal(new GetObjectiveGoal());
		addBehaviour(new RecieveObservations());
	}
	
	protected void takeDown(){
		super.takeDown();
	}
	
	// GET NEW OBJECTIVE IN MAP GOAL
	public class GetObjectiveGoal implements Goal {
		List <Couple<String, List <Couple<Observation, Integer>>>> ob;

		public void setObservations(List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
			this.ob = ob;
		}
	}
	// GET NEW OBJECTIVE IN MAP PLAN
	public class GetObjectivePlan extends AbstractPlanBody {
		
		//Calcula la nueva posición a la que se moverá el agente
		public String getNextPosition() {
			BeliefBase bb = getBeliefBase();
			String next = "";
			String randNext="";
			List <String> possibleMoves = new ArrayList<>();
			double maxScore, nextK, nextEx, nextEx2, nextVal;
			double randScore, randK, randEx, randEx2, randVal;

			maxScore=nextK=nextEx=nextEx2=nextVal=0.0;
			randScore=randK=randEx=randEx2=randVal=0.0;
			
			HashMap<String,Integer> locmap = (HashMap<String,Integer>) bb.getBelief("visitedNodes").getValue();
			String currentPos = (String) bb.getBelief("currentPosition").getValue();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = 
					(List <Couple<String, List <Couple<Observation, Integer>>>>)bb.getBelief("observations").getValue();
			
			double n = locmap.size();
			Double oldSuma = (Double) bb.getBelief("suma").getValue();
			
			Random rand = new Random();
			int pos = rand.nextInt(ob.size());
					
			int index=0;		
			for(Couple<String, List<Couple<Observation, Integer>>> o : ob) {
				
				boolean possible = true;
				for (Couple<Observation, Integer> elem : o.getRight()) {
					if (elem.getLeft().equals(Observation.WIND)) possible = false;
				}
				
					
				double new_val;
				double val;

				String id = o.getLeft();
				if(!locmap.containsKey(id)) val = 0.0;
				else val = Double.valueOf(locmap.get(id));

				new_val = val+1.0;

				ArrayList<Double> finalStdParam = new ArrayList<Double>();
				Double newScore = this.getNewScore(n, val, new_val, finalStdParam);
				
				if((newScore >= maxScore) && possible && !id.equals(currentPos)) {
									
					maxScore = newScore;
					nextVal = new_val;
					nextK = finalStdParam.get(0);
					nextEx = finalStdParam.get(1);
					nextEx2 = finalStdParam.get(2);
					next = id;
					
				}else if(index == pos) {
				
					randScore = newScore;
					randVal = new_val;
					randK = finalStdParam.get(0);
					randEx = finalStdParam.get(1);
					randEx2 = finalStdParam.get(2);
					randNext = id;

				}
				index++;
			}
			System.out.println(next);
			this.saveValueBB(oldSuma+1.0, "suma", bb);
			if(!next.equals("")) {
				locmap.put(next, (int)(nextVal));
				this.saveValueBB(locmap, "visitedNodes", bb);
				this.saveValueBB(nextK, "K", bb);
				this.saveValueBB(nextEx, "Ex", bb);
				this.saveValueBB(nextEx2, "Ex2", bb);
				return next;
			}else {
				locmap.put(randNext, (int)(randVal));
				System.out.println("El explorador se arriesga al viento!");
				this.saveValueBB(locmap, "visitedNodes", bb);
				this.saveValueBB(randK, "K", bb);
				this.saveValueBB(randEx, "Ex", bb);
				this.saveValueBB(randEx2, "Ex2", bb);	
			}
			return randNext;
		}
		
		private Double getNewScore(Double size, Double val, Double new_val, ArrayList<Double> finalStdParam) {
			BeliefBase bb = getBeliefBase();
			Double oldSuma = (Double) bb.getBelief("suma").getValue();
			Double K = (Double) bb.getBelief("K").getValue();
			Double Ex = (Double) bb.getBelief("Ex").getValue();
			Double Ex2 = (Double) bb.getBelief("Ex2").getValue();
			ArrayList<Double> newStdParam = this.delElemStd(K, Ex, Ex2, size, val);
			ArrayList<Double> newStdParam2 = this.addElemStd(newStdParam, size-1.0, new_val);
			Double newStd = Math.sqrt(this.getVariance(size,newStdParam2));
			Double newMean = (oldSuma+1.0)/(size+1.0);
			Double newScore = (newMean*newMean)/(1.0+newStd);
			for(Double d:newStdParam2)finalStdParam.add(d);
			return newScore/val;
			
		}
		
		private ArrayList<Double> delElemStd(Double K, Double Ex, Double Ex2, Double size,  Double elem) {
			
			ArrayList<Double> newStdParam = new ArrayList<Double>();
			if(size > 0) {
				size-=1;
				BeliefBase bb = getBeliefBase();
				
				Ex-=elem-K;
				Ex2 -= (elem - K)*(elem - K);
				newStdParam.add(K);
				newStdParam.add(Ex);
				newStdParam.add(Ex2);
			}else {
				newStdParam.add(0.0);
				newStdParam.add(0.0);
				newStdParam.add(0.0);
				
			}
			
			return newStdParam;
		}
		
		private ArrayList<Double> addElemStd(ArrayList<Double> oldStdParam, Double size, Double elem) {
			
			size +=1.0;
			Double K = oldStdParam.get(0);
			Double Ex = oldStdParam.get(1);
			Double Ex2 = oldStdParam.get(2);
			BeliefBase bb = getBeliefBase();
			if(size == 0) K = elem;
			Ex+=elem-K;
			Ex2 += (elem - K)*(elem - K);
			ArrayList<Double> newStdParam = new ArrayList<Double>();
			newStdParam.add(K);
			newStdParam.add(Ex);
			newStdParam.add(Ex2);
			return newStdParam;
			
			
		}
		
		
		
		private Double getVariance(Double size, ArrayList<Double> newStdParam) {
			
			if(size <= 1.0) return 0.0;
			
			BeliefBase bb = getBeliefBase();
			Double K = newStdParam.get(0);
	
			Double Ex = newStdParam.get(1);
			Double Ex2 = newStdParam.get(2);
			Double newVar = (Ex2-(Ex*Ex)/size)/(size-1);
			return newVar < 0.0 ? 0.0 : newVar;
			
			
		}
		
		
		private void saveValueBB(Object val, String name, BeliefBase bb) {
			TransientBelief newVal = new TransientBelief(name, val);
			bb.addOrUpdateBelief(newVal);
		}
		
		@Override
		public void action() {
			BeliefBase bb = getBeliefBase();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
			String currentNode = (String) bb.getBelief("currentPosition").getValue();
			MapRepresentation map = (MapRepresentation) (bb.getBelief("map").getValue());
			HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) (bb.getBelief("mapping").getValue());
			

			String nextMove = this.getNextPosition();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(body);
			msg.setSender(getAID());
			msg.addReplyTo(getAID());
            //msg.setContent(nextMove);
            msg.setConversationId("movimientos");
			Map <String, Object> message = new HashMap <>();
			message.put("nextMove", nextMove);
			message.put("map", map.getSerializableGraph());
			message.put("mapping", mapping);
            try {
				msg.setContentObject((Serializable) message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            Belief currentPosition = new TransientBelief("currentPosition", nextMove);
            bb.addOrUpdateBelief(currentPosition);
            myAgent.send(msg);
            
            setEndState(Plan.EndState.SUCCESSFUL);
		}
	}
	
	public class RecieveObservations extends CyclicBehaviour {
		private MessageTemplate tpl_reply, tpl_map, tpl_resource, tpl_agent;
		private ACLMessage msg_reply, msg_map, msg_resource, msg_agent;
		
        public void onStart() {
        	MessageTemplate tpl1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			MessageTemplate tpl2 = MessageTemplate.MatchSender(body);
			MessageTemplate tpl = MessageTemplate.and(tpl1, tpl2);
			
			tpl_reply = MessageTemplate.and(tpl, MessageTemplate.MatchConversationId("movimientos"));
			tpl_map = MessageTemplate.and(tpl, MessageTemplate.MatchConversationId("mapa"));
			tpl_resource = MessageTemplate.and(tpl, MessageTemplate.MatchConversationId("recursos"));
			tpl_agent = MessageTemplate.and(tpl, MessageTemplate.MatchConversationId("agentes"));
        }
        
        public void mergeResources (HashMap<String, Couple <Long, HashMap<Observation, Integer>>> m1, HashMap<String, Couple <Long, HashMap<Observation, Integer>>> m2) {
        	HashMap<String, Couple <Long, HashMap<Observation, Integer>>> res = new HashMap<>();
        	for (Map.Entry<String, Couple<Long, HashMap<Observation, Integer>>> e : m1.entrySet()) {
        		if (m2.containsKey(e.getKey())) {
        			if (!e.getValue().getRight().containsKey(Observation.GOLD) || m2.get(e.getKey()).getRight().containsKey(Observation.GOLD)) {
        				if (e.getValue().getLeft() >= m2.get(e.getKey()).getLeft()) res.put(e.getKey(), e.getValue());
        				else res.put(e.getKey(), m2.get(e.getKey()));
        			}
        		}
        		else res.put(e.getKey(), e.getValue());
        	}
        	
        	for (Map.Entry<String, Couple<Long, HashMap<Observation, Integer>>> e : m2.entrySet()) 
        		if (!res.containsKey(e.getKey())) res.put(e.getKey(), e.getValue());
        	
        	Capability c = getCapability();
    		BeliefBase bb = c.getBeliefBase();
    		Belief mapping = new TransientBelief("mapping", res);
    		bb.addOrUpdateBelief(mapping);
        }
        
        public void action() {
        	boolean in_one = false;
        	msg_reply = myAgent.receive(tpl_reply);     

        	if (msg_reply != null) {
            	in_one=true;
                try {
                	System.out.println("Recibo observaciones!");
                	Map <String, Object> rep = (Map <String, Object>) msg_reply.getContentObject();
					Capability c = getCapability();
					BeliefBase bb = c.getBeliefBase();
					boolean can_move = (boolean) rep.get("CAN_MOVE");
					
					if (!can_move) {
                		String current = (String) rep.get("CURRENT_POSITION");
                		Belief currPos = new TransientBelief("currentPosition", current);
						bb.addOrUpdateBelief(currPos);
                	}
                	else {
						List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) (rep.get("OBSERVATIONS"));
						
						Belief observations = new TransientBelief("observations", ob);
						bb.addOrUpdateBelief(observations);
                	}
					setNewNodesOntology();
					addGoal(new GetObjectiveGoal());
				} catch (UnreadableException e) { e.printStackTrace(); }
            
	            msg_map = myAgent.receive(tpl_map);     
	            if (msg_map != null) {
	            	try {
						SerializableSimpleGraph<String,MapAttribute> new_map = (SerializableSimpleGraph<String,MapAttribute>) msg_map.getContentObject();
						Capability c = getCapability();
						BeliefBase bb = c.getBeliefBase();
						
						MapRepresentation map = (MapRepresentation) bb.getBelief("map").getValue();
						
						map.mergeMap(new_map);
						Belief mapUpdate = new TransientBelief("map", map);
						
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	in_one = true;
	            }
	            
	            msg_resource = myAgent.receive(tpl_resource);  
	            if (msg_resource != null) {
					
	            	try {
						HashMap<String, Couple <Long, HashMap<Observation, Integer>>> m2 = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) msg_resource.getContentObject();
						if (m2 != null) {
							Capability c = getCapability();
							BeliefBase bb = c.getBeliefBase();
							
							HashMap<String, Couple <Long, HashMap<Observation, Integer>>> m1 =(HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) bb.getBelief("mapping").getValue();
							mergeResources(m1, m2);
						}
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	in_one = true;
	            }
	            
	            msg_agent = myAgent.receive(tpl_agent);
	            if (msg_agent != null) {
	            	try {
	            		Capability c = getCapability();
						BeliefBase bb = c.getBeliefBase();
						
						Map<String, Object> agents = (Map<String, Object>) msg_agent.getContentObject();
						HashMap<AID, Couple<Long, String>> agent_pos = (HashMap<AID, Couple<Long, String>>) agents.get("agent_pos");
						
						Belief bAgentPos = new TransientBelief("agent_positions", agent_pos);
						
						bb.addOrUpdateBelief(bAgentPos);
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	
	            	in_one = true;
	            }
	            
	            //if(!in_one) { block(); }
	        }
        }
	}
	
	public class InformAgentGoal implements Goal{
		
		private AID otherAgent;
		
		public InformAgentGoal(AID ag) {
			
			super();
			otherAgent = ag;
		}
		public AID getOtherAgent() {return otherAgent;}
		public void setOtherAgent(AID newAgent) {otherAgent = newAgent;}
		
	}
	
	public class InformAgentPlan extends AbstractPlanBody{

		@Override
		public void action() {
			InformAgentGoal ia = (InformAgentGoal) getGoal();
			AID receiver = ia.getOtherAgent();
			
			BeliefBase bb = getBeliefBase();
			Map<String, Boolean> agmap = (Map<String, Boolean>) bb.getBelief("Agents").getValue();
			agmap.put(receiver.getName(), true);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(body);
			msg.addReplyTo(this.getAgent().getAID());
			msg.setConversationId("comunicacion");
			msg.setSender(myAgent.getAID());
			Couple<AID, String> cpl = new Couple<AID,String>(receiver, "Te mando mi mapa. Aceptalo!");
			try {
				msg.setContentObject(cpl);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//send(msg);
			TransientBelief newb = new TransientBelief("Agentes", agmap);
			bb.addOrUpdateBelief(newb);
			setEndState(Plan.EndState.SUCCESSFUL);
			
		}
		
	}
	
}
