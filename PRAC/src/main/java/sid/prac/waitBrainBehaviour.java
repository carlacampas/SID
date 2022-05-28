package sid.prac;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class waitBrainBehaviour extends CyclicBehaviour {
	Explorer myexp = (Explorer) myAgent;
	MessageTemplate tpl = MessageTemplate.MatchSender(myexp.getBrains());
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(tpl);
		if(msg != null) {
			
			String cont = msg.getContent();
			
			
			
		}else block();

	}

}
