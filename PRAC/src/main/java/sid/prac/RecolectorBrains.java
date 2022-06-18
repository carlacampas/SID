package sid.prac;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import bdi4jade.core.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import bdi4jade.belief.*;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
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
	
	public void setNewNodesOntology () {
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
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
		
		//PROPERTIES
		Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
		Property oroProperty = model.createDatatypeProperty(BASE_URI + "#Has_oro");
		Property diamanteProperty = model.createDatatypeProperty(BASE_URI + "#Has_diamante");
		Property obstaculoProperty = model.createDatatypeProperty(BASE_URI + "#Has_viento");
		
		Individual currentNode = nodeClass.createIndividual(BASE_URI + "#Node" + current);
		map.addNewNode(current);
		
		RDFNode s_oro = currentNode.getPropertyValue(oroProperty);
		if (s_oro != null) currentNode.removeProperty(oroProperty, s_oro);
		
		RDFNode s_diamante = currentNode.getPropertyValue(oroProperty);
		if (s_diamante != null) {
			currentNode.removeProperty(diamanteProperty, s_diamante);
		}
		
		// add all new nodes
		for (Couple <String, List<Couple<Observation, Integer>>> o : ob) {
			boolean open = true;
			boolean agent = false;
			Individual adjacentNode = nodeClass.createIndividual(BASE_URI + "#Node" + o.getLeft());
			
			for (Couple<Observation, Integer> obs : o.getRight()) {
				if (obs.getLeft() == Observation.GOLD && obs.getRight() != 0) {
					Individual rec = oroClass.createIndividual(BASE_URI + "#Oro" + obs.getRight());
					adjacentNode.addProperty(oroProperty, rec);
				}
				else if (obs.getLeft() == Observation.DIAMOND && obs.getRight() != 0) {
					Individual rec = diamanteClass.createIndividual(BASE_URI + "#Diamante" + obs.getRight());
					adjacentNode.addProperty(diamanteProperty, rec);
				}
				else if (obs.getLeft() == Observation.WIND) {
					Individual rec = vientoClass.createIndividual(BASE_URI + "#Viento" + o.getLeft());
					adjacentNode.addProperty(obstaculoProperty, rec);
				}
				else if (obs.getLeft() == Observation.AGENTNAME) agent = true;
				else if (obs.getLeft() == Observation.LOCKPICKING && obs.getRight() == 0) open = false;
			}
			
			if (!o.getLeft().equals(current)) {
				currentNode.addProperty(nameProperty, adjacentNode);
				
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
		Belief model_updated = new TransientBelief("model", model);
		bb.addOrUpdateBelief(map_updated);
		bb.addOrUpdateBelief(mapping_updated);
		bb.addOrUpdateBelief(model_updated);
	}
	
	public void addExternalMap() {
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		MapRepresentation map = (MapRepresentation) (bb.getBelief("map").getValue());
		OntModel model = (OntModel) (bb.getBelief("model").getValue());
		
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
		Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
		
		SerializableSimpleGraph<String,MapAttribute> sg = map.getSerializableGraph();
		Set <SerializableNode<String, MapAttribute>> nodes = sg.getAllNodes();
		
		for (SerializableNode<String, MapAttribute> n : nodes) {
			Individual node_added = nodeClass.createIndividual(BASE_URI + "#Node" + n.getNodeId());
			Set<String> edges = sg.getEdges(n.getNodeId());
			for (String edge : edges) {
				Individual node_adjacent = nodeClass.createIndividual(BASE_URI + "#Node" + n.getNodeId());
				node_added.addProperty(nameProperty, node_adjacent);
			}
		}
		
		Belief model_updated = new TransientBelief("model", model);
		bb.addOrUpdateBelief(model_updated);
	}
	
	// create merge external resources with internal resources and then add to ontology
	public void addExternalResources() {
		// ONTOLOGY CLASSES
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
		OntClass diamanteClass = model.getOntClass(BASE_URI + "#Diamante");
		OntClass oroClass = model.getOntClass(BASE_URI + "#Oro");
		OntClass vientoClass = model.getOntClass(BASE_URI + "#Viento");
		
		//PROPERTIES
		Property nameProperty = model.createDatatypeProperty(BASE_URI + "#Adjacent");
		Property oroProperty = model.createDatatypeProperty(BASE_URI + "#Has_oro");
		Property diamanteProperty = model.createDatatypeProperty(BASE_URI + "#Has_diamante");
		Property obstaculoProperty = model.createDatatypeProperty(BASE_URI + "#Has_viento");
				
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		OntModel model = (OntModel) (bb.getBelief("model").getValue());
		HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) (bb.getBelief("mapping").getValue());
		
		for (Map.Entry<String, Couple<Long, HashMap<Observation, Integer>>> m : mapping.entrySet()) {
			Individual currentNode = nodeClass.createIndividual(BASE_URI + "#Node" + m.getKey());
			
			RDFNode s_oro = currentNode.getPropertyValue(oroProperty);
			if (s_oro != null) currentNode.removeProperty(oroProperty, s_oro);
			
			RDFNode s_diamante = currentNode.getPropertyValue(oroProperty);
			if (s_diamante != null) {
				currentNode.removeProperty(diamanteProperty, s_diamante);
			}
			
			for (Map.Entry<Observation, Integer> e : m.getValue().getRight().entrySet()) {
				if (e.getKey() == Observation.GOLD && e.getValue() != 0) {
					Individual rec = oroClass.createIndividual(BASE_URI + "#Oro" + e.getValue());
					currentNode.addProperty(oroProperty, rec);
				}
				else if (e.getKey() == Observation.DIAMOND && e.getValue() != 0) {
					Individual rec = diamanteClass.createIndividual(BASE_URI + "#Diamante" + e.getValue());
					currentNode.addProperty(diamanteProperty, rec);
				}
				else if (e.getKey() == Observation.WIND) {
					Individual rec = vientoClass.createIndividual(BASE_URI + "#Viento" + m.getKey());
					currentNode.addProperty(obstaculoProperty, rec);
				}
			}
		}
	}
	
	public void addAgents() {
		Capability c = getCapability();
		BeliefBase bb = c.getBeliefBase();
		OntModel model = (OntModel) (bb.getBelief("model").getValue());
		Set<AID> collectors = (Set<AID>) bb.getBelief("collectors").getValue();
		Set<AID> explorers = (Set<AID>) bb.getBelief("explorers").getValue();
		Set<AID> tanks = (Set<AID>) bb.getBelief("tanks").getValue();
		HashMap<AID, Couple<Long, String>> agent_pos = (HashMap<AID, Couple<Long, String>>) bb.getBelief("agent_positions").getValue();
		
		OntClass nodeClass = model.getOntClass(BASE_URI + "#Node");
		OntClass agentClass = model.getOntClass(BASE_URI + "#Agent");
		OntClass almacenamientoClass = model.getOntClass(BASE_URI + "#Almacenamiento");
		OntClass recolectorClass = model.getOntClass(BASE_URI + "#Recolector");
		OntClass exploradorClass = model.getOntClass(BASE_URI + "#Explorador");
		
		Property agenteProperty = model.createDatatypeProperty(BASE_URI + "#Agente_esta_en");
		
		for (Map.Entry<AID, Couple<Long, String>> ap : agent_pos.entrySet()) {
			String name = ap.getKey().getName();
			Individual agent;
			
			if (collectors.contains(name)) agent = recolectorClass.createIndividual(BASE_URI + "#Agent" + name);
			else if (explorers.contains(name)) agent = exploradorClass.createIndividual(BASE_URI + "#Agent" + name);
			else if (tanks.contains(name)) agent = almacenamientoClass.createIndividual(BASE_URI + "#Agent" + name);
			else agent = agentClass.createIndividual(BASE_URI + "#Agent" + name);
			
			agent.removeAll(agenteProperty);
			Individual node_added = nodeClass.createIndividual(BASE_URI + "#Node" + ap.getValue().getRight());
			agent.addProperty(agenteProperty, node_added);
		}
		
		Belief model_updated = new TransientBelief("model", model);
		bb.addOrUpdateBelief(model_updated);
		//Individual node_added = nodeClass.createIndividual(BASE_URI + "#Node" + n.getNodeId());
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
		
		public Integer check_num_away (String node, Set<String> find) {
			String[] possibilities = {"Node", "OneAway", "TwoAway", "ThreeAway", "FourAway", "FiveAway"};
			Individual curr = model.getIndividual(node);
			for (int i = 0; i < possibilities.length; i++) {
				String pos = possibilities[i];
				for (String f : find) {
					OntClass n_away = model.getOntClass(BASE_URI + "#" + pos + f);
					if (curr.hasOntClass(n_away)) return i;
				}
			}
			return -1;
		}
		
		public Couple<String, Integer> traverseOnt(String currentNode, Set<String> find) {//, Set <String> visited) {
			Individual curr = model.getIndividual(BASE_URI + "#Node" + currentNode);
		
			Property adj = model.createDatatypeProperty(BASE_URI + "#Adjacent");
            NodeIterator ni = curr.listPropertyValues(adj);
            Couple<String, Integer> best = new Couple("", -1);
            
            while (ni.hasNext()) {
            	RDFNode nextNode = ni.nextNode();
            	Integer check_away = check_num_away(nextNode.toString(), find);
            	if (check_away != -1 && (best.getRight() == -1 || best.getRight() > check_away)) {
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
			Set <String> find = new HashSet<>();
			
			if (treasure) find.add(type);
			if (almacenador) find.add("Almacenamiento");
			
			Couple<String, Integer> res = traverseOnt(currentPosition, find);
			if (res.getLeft() == "") return getClosestEmpty();
			
			return res.getLeft();
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
            	in_one = true;
                try {
                	Map <String, Object> rep = (Map <String, Object>) msg_reply.getContentObject();
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
            
            msg_map = myAgent.receive(tpl_map);     
            if (msg_map != null) {
            	try {
					SerializableSimpleGraph<String,MapAttribute> new_map = (SerializableSimpleGraph<String,MapAttribute>) msg_map.getContentObject();
					Capability c = getCapability();
					BeliefBase bb = c.getBeliefBase();
					
					MapRepresentation map = (MapRepresentation) bb.getBelief("map").getValue();
					
					map.mergeMap(new_map);
					Belief mapUpdate = new TransientBelief("map", map);
					addExternalMap();
					
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
						addExternalResources();
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
					Set<AID> collectors = (Set<AID>) agents.get("collectors");
					Set<AID> explorers = (Set<AID>) agents.get("explorers");
					Set<AID> tanks = (Set<AID>) agents.get("tanks");
					HashMap<AID, Couple<Long, String>> agent_pos = (HashMap<AID, Couple<Long, String>>) agents.get("agent_pos");
					
					Belief bCollectors = new TransientBelief("collectors", collectors);
					Belief bExplorers = new TransientBelief("explorers", explorers);
					Belief bTanks = new TransientBelief("tanks", tanks);
					Belief bAgentPos = new TransientBelief("agent_positions", agent_pos);
					
					bb.addOrUpdateBelief(bCollectors);
					bb.addOrUpdateBelief(bExplorers);
					bb.addOrUpdateBelief(bTanks);
					bb.addOrUpdateBelief(bAgentPos);
					
					addAgents();
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
