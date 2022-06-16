package sid.prac;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import bdi4jade.core.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.tuple.Couple;
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.env.Observation;
import java.util.Random;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
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
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import java.io.File;
import java.sql.Timestamp;

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
	Observation collectionType = Observation.ANY_TREASURE;
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
        
        Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
        Belief modelB = new TransientBelief("model", model);
        bb.addBelief(modelB);
    }
	
	public void releaseOntology() throws FileNotFoundException {
        System.out.println("· Releasing Ontology");
        if (!model.isClosed()) {
            model.write(new FileOutputStream(JENAPath + File.separator + MODIFIED_PREFIX + OntologyFile, false));
            model.close();
        }
    }
	
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
		System.out.println("Brains se despierta!");
		Object[] args = getArguments();
		if (args.length != 5) {
		      System.out.println("incorrect args");
		      doDelete();
		}
		
		body = (AID) args[0];
		collectionType = (Observation) args[1];
		String currentPosition = (String) args[2];
		ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) args[3];	
		Integer free = (Integer) args[4];
		loadOntology();
		
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Visitado");
		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + currentPosition);
		
		System.out.println("Brain waking up, successfully linked to body agent: " + body.getName());
		
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		Belief observations = new TransientBelief("observations", ob);
		Belief cType = new TransientBelief("collectionType", collectionType);
		Belief ontology = new TransientBelief("ontology", model);
		Belief currentNode = new TransientBelief("currentPosition", currentPosition);
		Belief freeSpace = new TransientBelief("freeSpace", free);
		Belief maxSpace = new TransientBelief("maxSpace", free);
		Belief mapping = new TransientBelief("mapping", new HashMap<String, Couple <Long, HashMap<Observation, Integer>>>());
		Belief map = new TransientBelief("map", new MapRepresentation());
		

		bb.addBelief(observations);
		bb.addBelief(cType);
		bb.addBelief(ontology);
		bb.addBelief(currentNode);
		bb.addBelief(freeSpace);
		bb.addBelief(maxSpace);
		bb.addBelief(mapping);
		bb.addBelief(map);
		
		Plan getGold = new DefaultPlan(GetTreasureGoal.class, GetTreasurePlan.class);
		
		setNewNodesOntology();
		c.getPlanLibrary().addPlan(getGold);
		this.addGoal(new GetTreasureGoal());
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
	
	// GET GOLD
	public class GetTreasureGoal implements Goal {
		List <Couple<String, List <Couple<Observation, Integer>>>> ob;
		OntModel model;
		Integer maxCapacity;
		Integer free;
		String currentPosition;
		String type;
		
		public void setObservations(List <Couple<String, List <Couple<Observation, Integer>>>> ob) {
			this.ob = ob;
		}
		
		public void setModel(OntModel model) { this.model = model; }
		
		public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
		
		public void setFree(Integer free) { this.free = free; }
		
		public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }
		
		public void setCollectionType(Observation collectionType) {
			if (collectionType == Observation.GOLD) type = "Oro";
			else type = "Diamante";
		}
		
		public Integer check_num_away (String node, String find) {
			String[] possibilities = {"Node", "OneAway", "TwoAway", "ThreeAway", "FourAway", "FiveAway"};
			Individual curr = model.getIndividual(node);
			for (int i = 0; i < possibilities.length; i++) {
				String pos = possibilities[i];
				OntClass n_away = model.getOntClass(BASE_URI + "#" + pos + find);
				if (curr.hasOntClass(n_away)) return i;
			}
			return -1;
		}
		
		public Couple<String, Integer> traverseOnt(String currentNode, String find) {//, Set <String> visited) {
			Individual curr = model.getIndividual(BASE_URI + "#Node" + currentNode);
		
			Property adj = model.createDatatypeProperty(BASE_URI + "#Adjacent");
            NodeIterator ni = curr.listPropertyValues(adj);
            Couple<String, Integer> best = new Couple("", -1);
            
            while (ni.hasNext()) {
            	RDFNode nextNode = ni.nextNode();
            	Integer check_away = check_num_away(nextNode.toString(), find);
            	if (check_away != -1 && (best.getRight() == -1 || best.getRight() > check_away)) {
            		System.out.println("HERE CLOSE TO GOLD!!");
            		String name = nextNode.toString().split("#Node")[1];
            		best = new Couple(name, check_away);
            	}
            }
            
            return best;
		}
		
		public String getClosestEmpty() {
			String next = "";
			Integer best = 0;
			List <String> possibleMoves = new ArrayList<>();
			for(Couple<String, List<Couple<Observation, Integer>>> o : ob) {
				Individual i = model.getIndividual(BASE_URI + "#Node" + o.getLeft());
				if (!i.hasOntClass(BASE_URI + "#Visitado")) {
					Integer q = 0;
					boolean possible = true;
					for (Couple<Observation, Integer> elem : o.getRight()) {
						if (elem.getLeft().equals(collectionType)) q += elem.getRight();
						else if (elem.getLeft().equals(Observation.WIND)) possible = false;
						else if (elem.getLeft().equals(Observation.AGENTNAME)) possible = false;
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
		
		
		
		public String getNextPosition() {
			boolean treasure = free != 0;
			boolean almacenador = free != maxCapacity;
			
			if (treasure && almacenador) {
				Couple<String, Integer> t = traverseOnt(currentPosition, type);
				Couple<String, Integer> a = traverseOnt(currentPosition, "Almacenamiento");
				
				if (a.getLeft() == "" && t.getLeft() == "") return getClosestEmpty();
				else if (a.getLeft() == "") return t.getLeft();
				else if (t.getLeft() == "") return a.getLeft();
				
				if (t.getRight() <= a.getRight()) return t.getLeft();
				return a.getLeft();
			}
			else if (treasure) {
				Couple<String, Integer> t = traverseOnt(currentPosition, type);
				if (t.getLeft() == "") return getClosestEmpty();
				return t.getLeft();
			}
			else if (almacenador) {
				Couple<String, Integer> a = traverseOnt(currentPosition, "Almacenamiento");
				if (a.getLeft() == "") return getClosestEmpty();
				return a.getLeft();
			}
			
			return getClosestEmpty();
		}
	}
	
	public class GetTreasurePlan extends AbstractPlanBody {
		
		@Override
		public void action() {
			BeliefBase bb = getBeliefBase();
			List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) bb.getBelief("observations").getValue();
			OntModel model = (OntModel) bb.getBelief("ontology").getValue();
			String currentNode = (String) bb.getBelief("currentPosition").getValue();
			Integer free = (Integer) bb.getBelief("freeSpace").getValue();
			Integer maxCapacity = (Integer) bb.getBelief("maxSpace").getValue();
			Observation collectionType = (Observation) bb.getBelief("collectionType").getValue();
			MapRepresentation map = (MapRepresentation) (bb.getBelief("map").getValue());
			HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) (bb.getBelief("mapping").getValue());
			
			
			GetTreasureGoal tg = (GetTreasureGoal) getGoal();
			
			tg.setObservations(ob);
			tg.setCollectionType(collectionType);
			tg.setModel(model);
			tg.setMaxCapacity(maxCapacity);
			tg.setFree(free);
			tg.setCurrentPosition(currentNode);
			
			String nextMove = tg.getNextPosition();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setConversationId("movimientos");
			msg.addReceiver(body);
			msg.setSender(getAID());
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
            send(msg);
            
            OntClass nodeClass = model.getOntClass(BASE_URI + "#Visitado");
    		Individual nodeInstance = nodeClass.createIndividual(BASE_URI + "#Node" + nextMove);
            
            Belief currentPosition = new TransientBelief("currentPosition", nextMove);
            bb.addOrUpdateBelief(currentPosition);
            
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
                	Map <String, Object> rep = (Map <String, Object>) msg.getContentObject();
                	boolean can_move = (boolean) rep.get("CAN_MOVE");
                	
                	Capability c = getCapability();
					BeliefBase bb = c.getBeliefBase();
					
					Integer free = (Integer) rep.get("BACKPACK_SPACE");
                	Belief freeSpace = new TransientBelief("freeSpace", free);
            		bb.addOrUpdateBelief(freeSpace);
            		
                	if (!can_move) {
                		String current = (String) rep.get("CURRENT_POSITION");
                		Belief currPos = new TransientBelief("currentPosition", current);
						bb.addOrUpdateBelief(currPos);
                	}
                	else {
						List <Couple<String, List <Couple<Observation, Integer>>>> ob = (List <Couple<String, List <Couple<Observation, Integer>>>>) rep.get("OBSERVATIONS");
						
						Belief observations = new TransientBelief("observations", ob);
						bb.addOrUpdateBelief(observations);
                	}
					
                	setNewNodesOntology();
					addGoal(new GetTreasureGoal());
				} catch (UnreadableException e) { e.printStackTrace(); }
            }
            else { block(); }
        }
	}
}
