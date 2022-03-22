package org.upc.edu.Behaviours;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


/*
FALTA: cyclic para ver si algun termometro envia un mensaje de que ha acabado
FALTA: implementar los puntos extra
FALTA: implementar alarm management
 */

public class SearchPingBehaviourAgent extends Agent
{
    float a, b;
    HashMap <AID, Float> prev_temp;

    public class Termostato extends TickerBehaviour
    {
        DFAgentDescription template;
        ServiceDescription templateSd;
        SearchConstraints sc;
        public Temperatura(Agent a, long timeout)
        {
            super(a,timeout);
            setAgent(a);
        }

        public void onStart() {
            template = new DFAgentDescription();
            templateSd = new ServiceDescription();
            templateSd.setType("Termometro");
            template.addServices(templateSd);
            sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));
        }

        public void onTick() {
            try {
                DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);
                if (results.length > 0) {
                    DFAgentDescription dfd = results[0];
                    AID provider = dfd.getName();

                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(provider);
                    msg.setContent("temperature");
                    send(msg);
                    
                    
                }
                else {
                    System.out.println("No Agent Found");
                    //implement different system
                }
            } catch (Exception e) {}
        }
    }

    protected void setup()
    {
        Object[] args = getArguments();
        if (args.length != 2) {
            System.out.println("Wrong number of parameters for thermometer inicialization.");
            exit();
        }
        float a = args[0].floatValue();
        float b = args[1].floatValue();
    }
}
