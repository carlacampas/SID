package sid.prac;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.knowledge.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
	Observation collectType;
	

	public MapRepresentation getMap() { return map; }
	public void setup() {
		super.setup();

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
		
		DFAgentDescription template =  new DFAgentDescription();
		ServiceDescription sd_template = new ServiceDescription();
		sd_template.setType(type);
        template.addServices(sd_template);
		Behaviour b = new SubscriptionInitiator(this, 
			DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					// do something
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		  };
		 
		lb.add(b);

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
		MessageTemplate tpl, tplExt, tplCom;
        ACLMessage msg, msgExt, msgCom;

		public void onStart() {
			MessageTemplate tpl1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			MessageTemplate tpl2 = MessageTemplate.MatchSender(brains);
			MessageTemplate tpl3 = MessageTemplate.MatchConversationId("movimientos");
			tpl = MessageTemplate.and(tpl1, tpl2);
			tpl = MessageTemplate.and(tpl, tpl3);
			tplExt = tpl1;
			tplExt = MessageTemplate.and(tpl1, MessageTemplate.not(tpl2));
			tpl3 = MessageTemplate.MatchConversationId("comunicacion");
			tplCom = MessageTemplate.and(tpl1, tpl2);
			tplCom = MessageTemplate.and(tplCom, tpl3);
			notifyAgents();
        }
		
		
		public void notifyAgents() {
			System.out.println("Empiezo a buscar agentes. Iniciando protocolo de Hola!");
			DFAgentDescription templateExplo = new DFAgentDescription();
			ServiceDescription templateSdExplo = new ServiceDescription();
			templateSdExplo.setType("agentExplo");
			templateExplo.addServices(templateSdExplo);
			
			DFAgentDescription templateCol = new DFAgentDescription();
			ServiceDescription templateSdCol = new ServiceDescription();
			templateSdCol.setType("agentCollect");
			templateCol.addServices(templateSdCol);
			
			try {
				DFAgentDescription[] results = DFService.search(myAgent, templateExplo);
				if(results.length>0) {
					for(DFAgentDescription d : results) {
						if(!d.getName().equals(myAgent.getAID())) {
							AID rcv = d.getName();
							AbstractDedaleAgent ag = (AbstractDedaleAgent) myAgent;
							ACLMessage nmsg = new ACLMessage(ACLMessage.INFORM);
							nmsg.addReceiver(rcv);
							nmsg.setContent("Hola!");
							nmsg.setSender(myAgent.getAID());
							ag.sendMessage(nmsg);
						}
					}
				}
				
				results = DFService.search(myAgent, templateCol);
				if(results.length>0) {
					for(DFAgentDescription d : results) {
						if(!d.getName().equals(myAgent.getAID())) {
							AID rcv = d.getName();
							AbstractDedaleAgent ag = (AbstractDedaleAgent) myAgent;
							ACLMessage nmsg = new ACLMessage(ACLMessage.INFORM);
							nmsg.addReceiver(rcv);
							nmsg.setContent("Hola!");
							nmsg.setSender(myAgent.getAID());
							ag.sendMessage(nmsg);
						}
					}
				}
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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

        public void action() {
        	
            msg = myAgent.receive(tpl);
            msgCom = myAgent.receive(tplCom);
            msgExt = myAgent.receive(tplExt);
            
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
	                				this.notifyAgents();
                					break;
	                			}
                	fp = sumFreeSpace(getBackPackFreeSpace());

                	System.out.println("Observations: " + ob.toString());

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
