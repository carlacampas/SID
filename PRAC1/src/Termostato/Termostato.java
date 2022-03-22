package sid.prac1;
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
    // Parametres d'entrada
    float a, b;
    // Hasmap amb tots els termostat i les seves temperatures
    HashMap <String, Float> prev_temp = new HashMap <String, Float>();
    // Mitjana de les temperatures
    float average;

    public class RecibirTemperaturas extends TickerBehaviour
    {
        public RecibirTemperaturas(Agent a, long timeout)
        {
            super(a,timeout);
        }

        public void onStart() {
        }

        // Ticker behaviour
        public void onTick() {

            // Busca d'agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            //templateSd.setType("Termometro");
            //template.addServices(templateSd);
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(Long.valueOf(10));
            try {
                DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);
                //System.out.println ("here " + results.length);

                if (results.length > 0) {   // nombre de termometres trobats
                    for(int i = 0; i<results.length; ++i){
                        DFAgentDescription dfd = results[i];
                        AID provider = dfd.getName();

                        // Enviem missatge al termometre perque ens dongui la temperatura
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(provider);
                        msg.setLanguage("English");
                        msg.setContent("temperature");
                        send(msg);

                        // Rebem resposta del termometre
                        MessageTemplate tpl = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                        msg = myAgent.receive(tpl);
                        //System.out.println(msg == null);
                        if (msg != null) {
                            String content = msg.getContent();
                            if (content != null) {
                                //System.out.println("RECIEVED TEMPERATURE: " + msg.getSender().getName() + " " + content);
                                prev_temp.put(msg.getSender().getName(), Float.parseFloat(content));   // actualitzem prev_temp
                            }
                        }
                        else {
                            block();
                        }
                    }
                    
                }
                else {
                    System.out.println("No Agent Found");
                    //implement different system
                }
            } catch (Exception e) {}
            // Escriptura i calcul de l'average
            average = 0;
            for (String i : prev_temp.keySet()) {
                System.out.println(i + " -> temperatura: " + prev_temp.get(i));
                average += prev_temp.get(i);
            }
            average /= prev_temp.size();
            System.out.println("Average: " + average);
            System.out.println("-----------------------------------");
        }
    }

    protected void setup()
    {
        // Parametres d'entrada
        Object[] args = getArguments();
        if (args.length != 2) {
            System.out.println("Wrong number of parameters for thermometer inicialization.");
            doDelete();
        }
        float a = Float.parseFloat(args[0].toString());
        float b = Float.parseFloat(args[1].toString());

        float ms = 1000;
        // Afegir behaviour a l'agent
        RecibirTemperaturas rt = new RecibirTemperaturas(this, Math.round(ms));
        this.addBehaviour(rt);
    }
}
