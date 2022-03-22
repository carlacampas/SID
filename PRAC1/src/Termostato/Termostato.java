package org.upc.edu.Behaviours;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;


/*
FALTA: ver si algun termometro ha caido en cada iteracion
FALTA: comparar con iteracion anterior valores
FALTA: recibir respuesta de temperatura
FALTA: implementar los puntos extra
FALTA: implementar alarm management
 */

public class Termostato extends Agent
{
    float a, b;
    HashMap <AID, Float> prev_temp;

    public class RecibirTemperaturas extends TickerBehaviour
    {
        DFAgentDescription template;
        ServiceDescription templateSd;
        SearchConstraints sc;
        public RecibirTemperaturas(Agent a, long timeout)
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
            sc.setMaxResults(Long.valueOf(10));
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

                    //recieve response from thermometer
                    MessageTemplate tpl = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    msg = myAgent.receive(tpl);
                    if (msg != null) {
                        String content = msg.getContent();
                        if (content != null) {
                            System.out.println("RECIEVED TEMPERATURE: " + content);
                        }
                    }
                    else {
                        block();
                    }
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
            System.exit(0);
        }
        float a = Float.parseFloat(args[0].toString());
        float b = Float.parseFloat(args[1].toString());

        float ms = 1000;
        RecibirTemperaturas rt = new RecibirTemperaturas(this, Math.round(ms));
        this.addBehaviour(rt);
    }
}
