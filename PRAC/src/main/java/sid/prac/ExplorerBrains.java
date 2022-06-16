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

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;

import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
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
	
	private static final String BASE_URI = "http://www.semanticweb.org/sid-prac3";
    private static final String MODIFIED_PREFIX = "modified_";
    
	OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;
    
	AID body;
	Observation collectionType;
    
	public ExplorerBrains () {
        this.JENAPath = "./";
        this.OntologyFile = "OntologiaPractica.owl";
        this.NamingContext = "prac3";
    }

	// Cargar ontologia
	public void loadOntology() {
        System.out.println("· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + OntologyFile);
        model.read(NamingContext);
        
        Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
        Belief modelB = new TransientBelief("model", model);
        bb.addBelief(modelB);
    }
	
	// Liberar ontologia
	public void releaseOntology() throws FileNotFoundException {
        System.out.println("· Releasing Ontology");
        if (!model.isClosed()) {
            model.write(new FileOutputStream(JENAPath + File.separator + MODIFIED_PREFIX + OntologyFile, false));
            model.close();
        }
    }
	
	// Crear nuevos nodos
	public void setNewNodesOntology () {
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		System.out.println(bb.toString());
		String current = (String) (bb.getBelief("currentPosition").getValue());
		OntModel model = (OntModel) bb.getBelief("model").getValue();
		MapRepresentation map = (MapRepresentation) (bb.getBelief("map").getValue());
		HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) (bb.getBelief("mapping").getValue());
		List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
		
		// ONTOLOGY CLASSES
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
		OntClass diamanteClass = model.getOntClass(BASE_URI + "#Diamante");
		OntClass oroClass = model.getOntClass(BASE_URI + "#Oro");
		OntClass vientoClass = model.getOntClass(BASE_URI + "#Viento");
		OntClass agentClass = model.getOntClass(BASE_URI + "#Agent");
		
		//PROPERTIES
		Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
		Property oroProperty = model.createDatatypeProperty(BASE_URI + "#Oro_esta_en");
		Property diamanteProperty = model.createDatatypeProperty(BASE_URI + "#Diamante_esta_en");
		Property obstaculoProperty = model.createDatatypeProperty(BASE_URI + "#Obstaculo_esta_en");
		Property agenteProperty = model.createDatatypeProperty(BASE_URI + "#Agente_esta_en");
		
		Individual currentNode = nodeClass.createIndividual(BASE_URI + "#Node" + current);
		map.addNewNode(current);
		
		Statement s_oro = currentNode.getProperty(oroProperty);
		if (s_oro != null)
			currentNode.removeProperty(oroProperty, s_oro.getResource());
		
		Statement s_diamante = currentNode.getProperty(diamanteProperty);
		if (s_diamante != null)
			currentNode.removeProperty(oroProperty, s_diamante.getResource());
		
		// add all new nodes
		for (Couple <String, List<Couple<Observation, Integer>>> o : ob) {
			boolean open = true;
			boolean agent = false;
			Individual adjacentNode = nodeClass.createIndividual(BASE_URI + "#Node" + o.getLeft());
			currentNode.addProperty(nameProperty, adjacentNode);
			
			for (Couple<Observation, Integer> obs : o.getRight()) {
				if (obs.getLeft() == Observation.GOLD && obs.getRight() != 0) {
					Individual rec = oroClass.createIndividual(BASE_URI + "#Oro" + obs.getRight());
					rec.addProperty(oroProperty, adjacentNode);
				}
				else if (obs.getLeft() == Observation.DIAMOND && obs.getRight() != 0) {
					Individual rec = diamanteClass.createIndividual(BASE_URI + "#Diamante" + obs.getRight());
					rec.addProperty(diamanteProperty, adjacentNode);
				}
				else if (obs.getLeft() == Observation.WIND) {
					Individual rec = vientoClass.createIndividual(BASE_URI + "#Viento" + o.getLeft());
					rec.addProperty(obstaculoProperty, adjacentNode);
				}
				else if (obs.getLeft() == Observation.AGENTNAME) {
					Individual rec = agentClass.createIndividual(BASE_URI + "#Agente" + o.getLeft());
					rec.addProperty(agenteProperty, adjacentNode);
					agent = true;
				}
				else if (obs.getLeft() == Observation.LOCKPICKING && obs.getRight() == 0) open = false;
			}
			
			if (!o.getLeft().equals(current)) {
				if (agent) map.addNode(o.getLeft(), MapAttribute.agent);
				else if (!open) map.addNode(o.getLeft(), MapAttribute.closed);
				else map.addNode(o.getLeft(), MapAttribute.open);
				map.addEdge(current, o.getLeft());
			}
		}
		
		Belief map_updated = new TransientBelief("map", map);
		Belief mapping_updated = new TransientBelief("mapping", mapping);
		Belief model_updated = new TransientBelief("model", model);
		bb.addOrUpdateBelief(map_updated);
		bb.addOrUpdateBelief(mapping_updated);
		bb.addOrUpdateBelief(model_updated);
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
		loadOntology();
		
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Visitado");
		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + currentPosition);
		
		System.out.println("Brain waking up, successfully linked to body agent: " + body.getName());
		
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		Belief observations = new TransientBelief("observations", ob);
		Belief ontology = new TransientBelief("ontology", model);
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
		bb.addBelief(ontology);
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
		try {
			releaseOntology();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		super.takeDown();
	}
	
	// GET NEW OBJECTIVE IN MAP GOAL
	public class GetObjectiveGoal implements Goal {
		List <Couple<String, List <Couple<Observation, Integer>>>> ob;
		OntModel model;

		public void setObservations(List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
			this.ob = ob;
		}

		public void setModel(OntModel model) { this.model = model; }

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
				if(index==pos) System.out.println("Ejecuto getNewScore con indice == pos");
				Double newScore = this.getNewScore(n, val, new_val, finalStdParam);
				System.out.println("newScore es: " + newScore);
				
				System.out.println("id es: " + id);
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
			
			this.saveValueBB(oldSuma+1.0, "suma", bb);
			System.out.println("next es antes de next.equals: " + next);
			if(!next.equals("")) {
				System.out.println("El explorador no se arriesga al viento!");
				locmap.put(next, (int)(nextVal));
				this.saveValueBB(locmap, "visitedNodes", bb);
				this.saveValueBB(nextK, "K", bb);
				this.saveValueBB(nextEx, "Ex", bb);
				this.saveValueBB(nextEx2, "Ex2", bb);
				System.out.println("locmap es: " + locmap);
				return next;
			}else {
				locmap.put(randNext, (int)(randVal));
				System.out.println("El explorador se arriesga al viento!");
				this.saveValueBB(locmap, "visitedNodes", bb);
				this.saveValueBB(randK, "K", bb);
				this.saveValueBB(randEx, "Ex", bb);
				this.saveValueBB(randEx2, "Ex2", bb);	
			}
			System.out.println("locmap es: " + locmap);
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
			System.out.println("Nuevo std: " + newStd);
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
			System.out.println("newStdParam en getVariance es: " + newStdParam);
			System.out.println("size es: " + size);
			Double newVar = (Ex2-(Ex*Ex)/size)/(size-1);
			return newVar < 0.0 ? 0.0 : newVar;
			
			
		}
		
		
		private void saveValueBB(Object val, String name, BeliefBase bb) {
			
			TransientBelief newVal = new TransientBelief(name, val);
			bb.addOrUpdateBelief(newVal);
			
			
		}

		private void sendOntology() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(body);
			msg.setSender(getAID());
            msg.setContent("Send info to other agents!"); 
            msg.setConversationId("comunicacion");
            send(msg);
			
		}
		
		@Override
		public void action() {
			BeliefBase bb = getBeliefBase();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
			OntModel model = (OntModel) bb.getBelief("ontology").getValue();
			String currentNode = (String) bb.getBelief("currentPosition").getValue();
			MapRepresentation map = (MapRepresentation) (bb.getBelief("map").getValue());
			HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) (bb.getBelief("mapping").getValue());
			

			String nextMove = this.getNextPosition();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(body);
			msg.setSender(getAID());
            //msg.setContent(nextMove);
            //msg.setConversationId("movimientos");
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
            
            
            System.out.println("NEXT MOVE: " + nextMove);
            
            OntClass nodeClass = model.getOntClass(BASE_URI + "#Visitado");
    		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + nextMove);
            
            Belief currentPosition = new TransientBelief("currentPosition", nextMove);
            bb.addOrUpdateBelief(currentPosition);
            //setNewNodesOntology(currentNode, model, ob);
            
            setEndState(Plan.EndState.SUCCESSFUL);
		}
	}
	
	public class RecieveObservations extends CyclicBehaviour {
		private MessageTemplate tplObs, tplAg, tplExt;
		private ACLMessage msgObs, msgAg, msgExt;
		
        public void onStart() {
        	MessageTemplate tpl1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			MessageTemplate tpl2 = MessageTemplate.MatchSender(body);
			MessageTemplate tpl3 = MessageTemplate.MatchConversationId("movimientos");
			tplObs = MessageTemplate.and(tpl1, tpl2);
			tplObs = MessageTemplate.and(tplObs, tpl3);
			
			tpl3 = MessageTemplate.MatchConversationId("comunicacion");
			tplAg = MessageTemplate.and(tpl1, tpl2);
			tplAg = MessageTemplate.and(tplAg, tpl3);
			
			tplExt = tpl1;
			
        }
        
        public void action() {
            msgObs = myAgent.receive(tplObs);
            msgAg = myAgent.receive(tplAg);
            msgExt = myAgent.receive(tplExt);
            

            if (msgObs != null) {
                try {
                	
                	Map <String, Object> rep = (Map <String, Object>) msgObs.getContentObject();
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
            }
            if(msgAg !=null) {
            	try {
            		AID rep = (AID) msgAg.getContentObject();
					addGoal(new InformAgentGoal(rep));
				} catch (UnreadableException e) { e.printStackTrace(); }
            }if(msgAg == null && msgObs==null) block(); 
        
            
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
			send(msg);
			TransientBelief newb = new TransientBelief("Agentes", agmap);
			bb.addOrUpdateBelief(newb);
			setEndState(Plan.EndState.SUCCESSFUL);
			
		}
		
	}
	
}
