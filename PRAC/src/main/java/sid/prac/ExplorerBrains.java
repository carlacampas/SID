package sid.prac;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sid.prac.RecolectorBrains.GetTreasureGoal;
import bdi4jade.core.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

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
	List <Couple<String, List <Couple<Observation, Integer>>>> ob;
    
	public ExplorerBrains () {
        this.JENAPath = "./";
        this.OntologyFile = "OntologiaPractica.owl";
        this.NamingContext = "prac3";
    }
	
	public void loadOntology() {
        System.out.println("· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + OntologyFile);
        model.read(NamingContext);
    }
	
	public void releaseOntology() throws FileNotFoundException {
        System.out.println("· Releasing Ontology");
        if (!model.isClosed()) {
            model.write(new FileOutputStream(JENAPath + File.separator + MODIFIED_PREFIX + OntologyFile, false));
            model.close();
        }
    }
	
	public OntModel setNewNodesOntology (String current, OntModel model, List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
		// ONTOLOGY CLASSES
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
		OntClass diamanteClass = model.getOntClass(BASE_URI + "#Diamante");
		OntClass oroClass = model.getOntClass(BASE_URI + "#Oro");
		OntClass vientoClass = model.getOntClass(BASE_URI + "#Viento");
		OntClass agentClass = model.getOntClass(BASE_URI + "#Agent");
		
		//PROPERTIES
		Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
		Property recrusoProperty = model.createDatatypeProperty(BASE_URI + "#Recurso_esta_en");
		Property obstaculoProperty = model.createDatatypeProperty(BASE_URI + "#Obstaculo_esta_en");
		Property agenteProperty = model.createDatatypeProperty(BASE_URI + "#Agente_esta_en");
		
		Individual currentNode = nodeClass.createIndividual(BASE_URI + "#Node" + current);
		
		// add all new nodes
		for (Couple <String, List<Couple<Observation, Integer>>> o : ob) {
			
			Individual adjacentNode = nodeClass.createIndividual(BASE_URI + "#Node" + o.getLeft());
			currentNode.addProperty(nameProperty, adjacentNode);
			
			for (Couple<Observation, Integer> obs : o.getRight()) {
				Individual rec;
				switch(obs.getLeft()) {
					case GOLD: 
						rec = oroClass.createIndividual(BASE_URI + "#Oro" + obs.getRight());
						adjacentNode.addProperty(recrusoProperty, rec);
						break;
					case DIAMOND:
						rec = diamanteClass.createIndividual(BASE_URI + "#Diamante" + obs.getRight());
						adjacentNode.addProperty(recrusoProperty, rec);
						break;
					case WIND:
						rec = diamanteClass.createIndividual(BASE_URI + "#Viento" + o.getLeft());
						adjacentNode.addProperty(obstaculoProperty, rec);
						break;
					case AGENTNAME:
						//find out que tipo de agente es
						rec = agentClass.createIndividual(BASE_URI + "#Agente" + obs.getRight());
						adjacentNode.addProperty(agenteProperty, rec);
					default:
						break;
				}
			}
		}
		return model;
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
		ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) args[3];		
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
		Belief sum = new TransientBelief("suma", 0);
		Belief std = new TransientBelief("std", 0);
		bb.addBelief(observations);
		bb.addBelief(ontology);
		bb.addBelief(currentNode);
		bb.addBelief(sum);
		bb.addBelief(std);
		
		
	}
	
	protected void takeDown(){
		try {
			releaseOntology();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		super.takeDown();
	}
	
	// GET GOLD
	public class GetObjectiveGoal implements Goal {
		List <Couple<String, List <Couple<Observation, Integer>>>> ob;
		OntModel model;

		public void setObservations(List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
			this.ob = ob;
		}

		public void setModel(OntModel model) { this.model = model; }

		public String getNextPosition(HashMap<String,Integer> locmap) {
			String next = "";
			Integer best = Integer.MAX_VALUE;
			List <String> possibleMoves = new ArrayList<>();
			int min;
			for(Couple<String, List<Couple<Observation, Integer>>> o : ob) {
				
				String id = o.getLeft();
				if(locmap.containsKey(id)){
					
					int cnt = locmap.get(id);
					locmap.put(id, cnt+1);
					
					
				}else locmap.put(id, 1);
				
			}
			if (best == 0) {
				if (possibleMoves.size() > 0) {
					Random rand = new Random();
					int pos = rand.nextInt(possibleMoves.size());
					next = possibleMoves.get(pos);
				}
				else {
					Random rand = new Random();
					int pos = rand.nextInt(ob.size());
					next = ob.get(pos).getLeft();
				}
			}
			return next;
		}
	}
	
	public class GetObjectivePlan extends AbstractPlanBody {
		
		@Override
		public void action() {
			BeliefBase bb = getBeliefBase();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
			OntModel model = (OntModel) bb.getBelief("ontology").getValue();
			String currentNode = (String) bb.getBelief("currentPosition").getValue();
			
			GetTreasureGoal tg = (GetTreasureGoal) getGoal();
			
			tg.setObservations(ob);
			tg.setModel(model);
			String nextMove = tg.getNextPosition();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(body);
			msg.setSender(getAID());
            msg.setContent(nextMove);
            send(msg);
            
            System.out.println("NEXT MOVE: " + nextMove);
            
            OntClass nodeClass = model.getOntClass(BASE_URI + "#Visitado");
    		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + nextMove);
            
            Belief currentPosition = new TransientBelief("currentPosition", nextMove);
            bb.addOrUpdateBelief(currentPosition);
            setNewNodesOntology(currentNode, model, ob);
            
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
					
					//addGoal(new GetTreasureGoal());
				} catch (UnreadableException e) { e.printStackTrace(); }
            }
            else { block(); }
        }
	}
	
	
}
