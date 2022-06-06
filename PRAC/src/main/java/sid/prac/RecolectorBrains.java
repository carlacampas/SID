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
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.env.Observation;
import java.util.Random;
import java.util.ArrayList;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.rdf.model.NodeIterator;

public class RecolectorBrains extends SingleCapabilityAgent {
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
    
	public RecolectorBrains () {
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
	
	public OntModel setNewNodesOntology (OntModel model, List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
		
		// add all new nodes
		for (Couple <String, List<Couple<Observation, Integer>>> o : ob) {
			Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#" + o.getLeft());
		}
		return model;
	}
	
	protected void init() {
		System.out.println("Brains se despierta!");
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
		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#" + currentPosition);
		
		System.out.println("Brain waking up, successfully linked to body agent: " + body.getName());
		
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		Belief observations = new TransientBelief("observations", ob);
		Belief ontology = new TransientBelief("ontology", model);
		bb.addBelief(observations);
		bb.addBelief(ontology);
		
		Plan getGold = new DefaultPlan(GetTreasureGoal.class, GetTreasurePlan.class);
		
		c.getPlanLibrary().addPlan(getGold);
		this.addGoal(new GetTreasureGoal());
		addBehaviour(new RecieveObservations());
	}
	
	// GET GOLD
	public class GetTreasureGoal implements Goal {
		List <Couple<String, List <Couple<Observation, Integer>>>> ob;
		OntModel model;
		
		public void setObservations(List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
			this.ob = ob;
		}
		
		public void setModel(OntModel model) { this.model = model; }
		
		public String getNextPosition() {
			String next = "";
			Integer best = 0;
			List <String> possibleMoves = new ArrayList<>();
			for(Couple<String, List<Couple<Observation, Integer>>> o : ob) {
				Individual i = model.getIndividual(BASE_URI + "#" + o.getLeft());
				if (i == null || !i.hasOntClass(BASE_URI + "#Visitado")) {
					System.out.println("here node: " + o.getLeft());
					Integer q = 0;
					boolean possible = true;
					for (Couple<Observation, Integer> elem : o.getRight()) {
						if (elem.getLeft() == collectionType) q += elem.getRight();
						else if (elem.getLeft() == Observation.WIND) possible = false;
					}
					if (q > best) next = o.getLeft();
					else if (possible) possibleMoves.add(o.getLeft());
				}
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
	
	public class GetTreasurePlan extends AbstractPlanBody {
		
		@Override
		public void action() {
			BeliefBase bb = getBeliefBase();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
			OntModel model = (OntModel) bb.getBelief("ontology").getValue();
			
			ExtendedIterator<Individual> its = model.listIndividuals();
			System.out.println("Individuals: " + its.toList().size());
			for (Individual ind : its.toList()) {
				System.out.println(ind.getLocalName());
			}
			
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
    		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#" + nextMove);
            
            setNewNodesOntology(model, ob);
            
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
