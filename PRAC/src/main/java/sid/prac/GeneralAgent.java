package sid.prac;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.knowledge.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.wrapper.AgentContainer;
import jade.domain.FIPAException;
import jade.domain.DFService;

import sid.prac.ExplorerBrains;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

import java.util.HashSet;
import java.util.HashMap;

public class GeneralAgent extends AbstractDedaleAgent {
	private MapRepresentation map;
	private String type;
	private AID brains;
	private Observation collectType;
	private Set<AID> collectors = new HashSet<AID> ();
	private Set<AID> explorers = new HashSet<AID> ();
	private Set<AID> tanks = new HashSet<AID> ();
	private HashMap<AID, Couple<Long, String>> agent_pos;
	private MessageTemplate all_senders;

	public MapRepresentation getMap() { return map; }
	public void setup() {
		super.setup();
		all_senders = MessageTemplate.and(MessageTemplate.not(MessageTemplate.MatchSender(brains)), MessageTemplate.not(MessageTemplate.MatchSender(getAID())));

		List<Behaviour> lb=new ArrayList<Behaviour>();

		lb.add(new OneShotBehaviour() {   // Mirar el rol del agente
			public void action () {
				System.out.println("one shot behaviour");
				int sum = sumFreeSpace(getBackPackFreeSpace());

				if (sum == 0) {    // Agente Explorador
					type = "agentExplo";
					addExplorerBrains();
					System.out.println("I am an explorer!");
				}
				else {            // Agente Recolector
					type = "agentCollect";
					collectType = getMyTreasureType();
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
		
		// crear subscripcion a agentes exploradores
		DFAgentDescription template =  new DFAgentDescription();
		ServiceDescription sd_template = new ServiceDescription();
		sd_template.setType("agentExplo");
        template.addServices(sd_template);
		Behaviour b_explo = new SubscriptionInitiator(this, 
			DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					for (DFAgentDescription d : dfds) {
						explorers.add(d.getName());
						all_senders = MessageTemplate.and(all_senders, MessageTemplate.MatchSender(d.getName()));
					}
					// do something
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		  };
		 
		lb.add(b_explo);
		
		sd_template.setType("agentCollect");
        template.addServices(sd_template);
		Behaviour b_collect = new SubscriptionInitiator(this, 
			DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					for (DFAgentDescription d : dfds) {
						collectors.add(d.getName());
						all_senders = MessageTemplate.and(all_senders, MessageTemplate.MatchSender(d.getName()));
					}
					// do something
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		  };
		 
		lb.add(b_collect);
		
		sd_template.setType("agentTanker");
        template.addServices(sd_template);
		Behaviour b_tanker = new SubscriptionInitiator(this, 
			DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					for (DFAgentDescription d : dfds) {
						tanks.add(d.getName());
						all_senders = MessageTemplate.and(all_senders, MessageTemplate.MatchSender(d.getName()));
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		  };
		 
		lb.add(b_collect);

		lb.add(new RecieveNextMove());
		addBehaviour(new startMyBehaviours(this,lb));
		System.out.println("here, done with behaviour");
	}

	// Añadimos agente BDI Explorer
	public void addExplorerBrains() {
		System.out.println("Llego a setup de Explorer");
		AgentContainer ac = getContainerController();
		final Object[] args = {getAID(), collectType, getCurrentPosition(), observe()};
		try {
			AgentController ag = ac.createNewAgent("brainy_" + getName(), "sid.prac.ExplorerBrains", args);
			ag.start();
			brains = new AID(ag.getName(), AID.ISGUID);

		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Añadimos agente BDI Recolector
	public void addRecolectorBrains() {
		System.out.println("RECOLECTOR PUEDE RECOGER: " + collectType.toString());

		final Object[] args = {getAID(), collectType, getCurrentPosition(), observe(), sumFreeSpace(getBackPackFreeSpace())};

		System.out.println("Llego a setup de Recolector");

		AgentContainer ac = getContainerController();
		try {
			AgentController ag = ac.createNewAgent("brainy_" + getName(), "sid.prac.RecolectorBrains", args);
			ag.start();
			brains = new AID(ag.getName(), AID.ISGUID);

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

	// Recivimos mensaje del Brains esperando la accion
	public class RecieveNextMove extends CyclicBehaviour {
		MessageTemplate tpl, tpl_map, tpl_resource, tpl_positions;
        ACLMessage msg, msg_map, msg_resource, msg_positions;
        SerializableSimpleGraph<String,MapAttribute> map;
        HashMap<String, Couple <Long, HashMap<Observation, Integer>>> mapping;

		public void onStart() {
			MessageTemplate tpl1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			MessageTemplate tpl2 = MessageTemplate.MatchSender(brains);
			tpl = MessageTemplate.and(tpl1, tpl2);
			
			tpl_map = MessageTemplate.and(all_senders, MessageTemplate.MatchOntology("mapTopology"));
			tpl_resource = MessageTemplate.and(all_senders, MessageTemplate.MatchOntology("resourceInformation"));
			tpl_positions = MessageTemplate.and(all_senders, MessageTemplate.MatchOntology("agentPositions"));
        }
		
		public void handle_current_node (Couple<String, List<Couple<Observation, Integer>>> o) {
			boolean myItem = false, lockOpen = false;
        	int strength = 0, lockPicking = 0;
			for (Couple<Observation, Integer> obs : o.getRight()) {
				if (obs.getLeft() == collectType) {
					myItem = true;
				}
				else {
					switch(obs.getLeft()) {
						case LOCKSTATUS:
							lockOpen = obs.getRight() == 1;
							break;
						case LOCKPICKING:
							lockPicking = obs.getRight();
							break;
						case STRENGH:
							strength = obs.getRight();
							break;
						default:
							break;
					}
				}
			}
			if (!myItem) return;

			System.out.println("BEFORE: " + getBackPackFreeSpace());
			Set<Couple<Observation, Integer>> exp = getMyExpertise();

			if (lockOpen) pick();
			else {
				boolean can_open = true;
				for (Couple<Observation, Integer> e : exp) {
					if (e.equals(Observation.LOCKPICKING)) can_open = can_open && (e.getRight() >= lockPicking);
					else if (e.equals(Observation.STRENGH)) can_open = can_open && (e.getRight() >= strength);
				}

				if (!can_open) return;
				openLock(Observation.ANY_TREASURE);
				pick();
			}
			System.out.println("AFTER: " + getBackPackFreeSpace());
		}
		
		public void sendExternalMessages() {
			ACLMessage msg_topology = new ACLMessage(ACLMessage.INFORM);
			ACLMessage msg_resourceInfo = new ACLMessage(ACLMessage.INFORM);
			ACLMessage msg_agentPositions = new ACLMessage(ACLMessage.INFORM);
			msg_topology.setOntology("mapTopology");
			msg_resourceInfo.setOntology("resourceInformation");
			msg_agentPositions.setOntology("agentPositions");
			
			for (AID a : collectors) {
				msg_topology.addReceiver(a);
				msg_resourceInfo.addReceiver(a);
				msg_agentPositions.addReceiver(a);
			}
			
			for (AID a : explorers) {
				msg_topology.addReceiver(a);
				msg_resourceInfo.addReceiver(a);
				msg_agentPositions.addReceiver(a);
			}
			
			for (AID a : tanks) {
				msg_topology.addReceiver(a);
				msg_resourceInfo.addReceiver(a);
				msg_agentPositions.addReceiver(a);
			}
			msg_topology.setSender(getAID());
			msg_resourceInfo.setSender(getAID());
			msg_agentPositions.setSender(getAID());
			
			try {
				msg_topology.setContentObject((Serializable) map);
				msg_resourceInfo.setContentObject((Serializable) mapping);
				msg_agentPositions.setContentObject((Serializable) agent_pos);
			} catch (IOException e) { e.printStackTrace(); }
            
            sendMessage(msg_topology);
            sendMessage(msg_resourceInfo);
            sendMessage(msg_agentPositions);
		}

        public void action() {
            msg = myAgent.receive(tpl);
            
            if (msg != null) {
                Map<String, Object> content = new HashMap<String, Object>();
				try {
					content = (Map<String, Object>) msg.getContentObject();
				} catch (UnreadableException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
                System.out.println("El cont. del mensaje es: " + content.get("nextMove"));
               
                if (content != null) {
                	boolean m = moveTo((String) content.get("nextMove"));
                	List <Couple<String, List <Couple<Observation, Integer>>>> ob = observe();

                	Integer fp = sumFreeSpace(getBackPackFreeSpace());
                	if (type.equals("agentCollect") && fp > 0)
	                	for (Couple<String, List <Couple<Observation, Integer>>> o : ob)
	                		if (content.equals(o.getLeft())) {
	                			handle_current_node(o);
	                			break;
	                		}
	                else if(type.equals("agentExplo"))
	                	for(Couple<String, List <Couple<Observation, Integer>>> o2 : ob)
	                		for(Couple<Observation,Integer> o3: o2.getRight())
	                			if(o3.getLeft() == Observation.STRENGH) {
	                				//this.notifyAgents();
                					break;
	                			}
                	fp = sumFreeSpace(getBackPackFreeSpace());

                	System.out.println("Observations: " + ob.toString());
                	map = (SerializableSimpleGraph<String,MapAttribute>) content.get("map");
                	mapping = (HashMap<String, Couple <Long, HashMap<Observation, Integer>>>) content.get("mapping");
                	sendExternalMessages();

                	Map <String, Object> pass_info = new HashMap <String, Object>();
                	pass_info.put("OBSERVATIONS", ob);
                	pass_info.put("CAN_MOVE", m);
                	pass_info.put("CURRENT_POSITION", getCurrentPosition());
                	if (type.equals("agentCollect")) pass_info.put("BACKPACK_SPACE", fp);

                	ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    try {
                    	reply.setContentObject((Serializable) pass_info);
                    } catch (Exception e) {}
                    send(reply);
                }
            }
            
            msg_map = myAgent.receive(tpl_map);
            if (msg_map != null) {
            	ACLMessage msg_brain = new ACLMessage(ACLMessage.INFORM);
            	msg_brain.addReceiver(brains);
            	msg_brain.setConversationId("mapa");
            	msg_brain.setSender(getAID());
            	try {
					msg.setContentObject(msg_map.getContentObject());
            	} catch (IOException | UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	send(msg);
            }
            
            msg_resource = myAgent.receive(tpl_resource);
            if (msg_resource != null) {
            	ACLMessage msg_brain = new ACLMessage(ACLMessage.INFORM);
            	msg_brain.addReceiver(brains);
            	msg_brain.setConversationId("recursos");
            	msg_brain.setSender(getAID());
            	try {
					msg.setContentObject(msg_resource.getContentObject());
				} catch (IOException | UnreadableException e1) { e1.printStackTrace(); }
            	send(msg);
            }
            
            msg_positions = myAgent.receive(tpl_positions);
            if (msg_positions != null) {
            	try {
					Map<AID, Couple<Long, String>> content = (HashMap<AID, Couple<Long, String>>) msg_positions.getContentObject();
					if (content != null) {
						for (Map.Entry<AID, Couple<Long, String>> e : content.entrySet()) {
							if (agent_pos.containsKey(e.getKey())) {
								if (agent_pos.get(e.getKey()).getLeft() < e.getValue().getLeft()) {
									agent_pos.put(e.getKey(), e.getValue());
								}
							}
							else agent_pos.put(e.getKey(), e.getValue());
						}
						
						ACLMessage msg_brain = new ACLMessage(ACLMessage.INFORM);
		            	msg_brain.addReceiver(brains);
		            	msg_brain.setConversationId("agentes");
		            	msg_brain.setSender(getAID());
		            	Map <String, Object> agent_info = new HashMap<>();
		            	agent_info.put("collectors", collectors);
		            	agent_info.put("explorers", explorers);
		            	agent_info.put("tanks", tanks);
		            	agent_info.put("agent_pos", agent_pos);
		            	msg_brain.setContentObject((Serializable) agent_info);
		            	send(msg_brain);
					}
	            	
				} catch (IOException | UnreadableException e) { e.printStackTrace(); }
            }
        }
	}

	// Suma capacidades
	public Integer sumFreeSpace(List<Couple<Observation, Integer>> bp) {
		Integer sum = 0;
		for (Couple<Observation, Integer> b : bp) {
			sum += b.getRight();
		}
		return sum;
	}
	
}
