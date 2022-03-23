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
import java.util.HashSet;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


/*
FALTA: completar checkTempValues --> decidir si los dejamos x iteraciones o para siempre
FALTA: implementar los puntos extra
 */

public class Termostato extends Agent
{
    // Parametres d'entrada
    private float a, b;

    // Mitjana de les temperatures
    private float average;

    // Hasmap amb tots els termostat i les seves temperatures
    HashMap <AID, Float> prev_temp = new HashMap <AID, Float>();

    // Hashmap amb els AIDs de termometres cancelats
    HashSet <AID> removed = new HashSet <AID> ();

    // Número de agents que s'han començat en el codi
    int num_term = 0;

    public class RecibirTemperaturas extends TickerBehaviour
    {
        public RecibirTemperaturas(Agent a, long timeout)
        {
            super(a,timeout);
        }

        public void onStart() {}

        private void checkTempValues (HashMap <AID, Float> temps) {
            //check new temperatures allign with old ones and remove thermometers that don't algin
            prev_temp = temps;
        }

        public void checkCorrectAverage () {
            if (a >= average || average >= b) {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription templateSd = new ServiceDescription();
                templateSd.setType("alarm-management");
                template.addServices(templateSd);
                SearchConstraints sc = new SearchConstraints();
                sc.setMaxResults(Long.valueOf(10));

                try {
                    DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("alarm-managenent inform message");
                    for (DFAgentDescription r : results) {
                        msg.addReceiver(r.getName());
                    }
                    send(msg);
                } catch (Exception e) {}
            }
        }

        // Ticker behaviour
        public void onTick() {
            // Inicialitzar map amb noves temperatures
            HashMap <AID, Float> new_temp = new HashMap <AID, Float> ();
            // Busca d'agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("Termometro");
            template.addServices(templateSd);
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(Long.valueOf(10));
            try {
                DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);

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
                                // actualitzem new_temp
                                new_temp.put(provider, Float.parseFloat(content));
                            }
                        }
                        else {
                            block();
                        }
                    }
                    checkTempValues(new_temp);
                }
                else {
                    // Si no se encuentra ningún agente valido, empezamos un agente nuevo desde codigo
                    System.out.println("No agent found. Initializing new agent");
                    AgentContainer ac = myAgent.getContainerController();
                    AgentController new_agent = ac.createNewAgent(("term-started-code " + String.valueOf(num_term)),
                                                                    "sid.prac1.Termometro", new Object[]{average, average, 0.5, 1});
                    new_agent.start();
                    num_term++;
                }
            } catch (Exception e) {}

            // Escriptura i calcul de l'average
            average = 0;
            for (AID i : prev_temp.keySet()) {
                System.out.println(i + " -> temperatura: " + prev_temp.get(i));
                average += prev_temp.get(i);
            }
            average /= prev_temp.size();
            System.out.println("Average: " + average);
            System.out.println("-----------------------------------");
            checkCorrectAverage();
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
        a = Float.parseFloat(args[0].toString());
        b = Float.parseFloat(args[1].toString());
        average = 0;

        float ms = 1000;
        // Afegir behaviour a l'agent
        RecibirTemperaturas rt = new RecibirTemperaturas(this, Math.round(ms));
        this.addBehaviour(rt);
    }
}
