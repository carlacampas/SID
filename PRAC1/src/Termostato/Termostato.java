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
import java.util.DoubleSummaryStatistics;

public class Termostato extends Agent
{
    // Parametres d'entrada
    private double a, b;

    // Mitjana de les temperatures
    private double average, sd, currentTemp;

    // HashMap ...
    HashMap <AID, Integer> correct_temp_counter = new HashMap <AID, Integer>();

    // ya ha conseguido los valores de algun temometro alguna vez
    boolean enteredOnce = false;

    private AgentController my_agent;
    
    public class RecibirTemperaturas extends TickerBehaviour
    {
        public RecibirTemperaturas(Agent a, long timeout)
        {
            super(a,timeout);
        }

        public void onStart() {}

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

        private void calcStats (HashMap <AID, Double> temps) {
            // primers calculs
            double sum = 0.0;
            for(Double x : temps.values()) sum=sum+x;

            average = sum/temps.size();
            sum = 0.0;

            for(Double x : temps.values()) sum+=Math.pow((x-average),2);
            sd=Math.sqrt(sum/(temps.size()-1));
        }

        // Ticker behaviour
        public void onTick() {
            // Inicialitzar map amb noves temperatures
            HashMap <AID, Double> temps = new HashMap <AID, Double> ();
            // Busca d'agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("Termometro");
            template.addServices(templateSd);
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(Long.valueOf(10));

            try {
                DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);

                if (results.length > 0) {   // num. de termometres trobats
                    if (results.length >= 2 && my_agent != null) { my_agent.kill(); my_agent = null; }
                    for(int i = 0; i<results.length; ++i){
                        DFAgentDescription dfd = results[i];
                        AID provider = dfd.getName();

                        // Enviem missatge al termometre perque ens dongui la temperatura
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(provider);
                        msg.setContent("temperature");
                        send(msg);

                        // Rebem resposta del termometre
                        MessageTemplate tpl = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                        msg = myAgent.receive(tpl);

                        if (msg != null) {
                            String content = msg.getContent();
                            // actualitzem temp
                            if (content != null) { temps.put(provider, Double.parseDouble(content)); }
                        }
                        else { block(); }
                    }
                    
                    if (temps.size() == 1 || my_agent != null) {
                        currentTemp = 0.0;
                        for (AID i : temps.keySet()) {
                            System.out.println(i + " -> temperatura: " + temps.get(i));
                            currentTemp += temps.get(i);
                        }
                    }
                    else {
                        calcStats(temps);
                        double new_avg = 0.0; currentTemp = 0.0;
                        int count = 0;
                        for (AID i : temps.keySet()) {
                            System.out.println(i + " -> temperatura: " + temps.get(i));
                            if (average - 3*sd <= temps.get(i) && temps.get(i) <= average + 3*sd) {
                                int x = 1;
                                if (correct_temp_counter.containsKey(i))
                                    x = correct_temp_counter.get(i) + 1;

                                correct_temp_counter.put(i, x);

                                if (correct_temp_counter.get(i) > 0) {
                                    new_avg += temps.get(i) * correct_temp_counter.get(i);
                                    count += correct_temp_counter.get(i);
                                }
                            } else {
                                int x = -1;
                                if (correct_temp_counter.containsKey(i))
                                    x = correct_temp_counter.get(i) - 1;
                                correct_temp_counter.put(i, x);
                            }
                        }
                        currentTemp = new_avg/count;
                    }
                    // Escriptura i calcul de l'average
                    if (currentTemp == 0.0) currentTemp = average;
                    
                    System.out.println("Average: " + currentTemp);
                    System.out.println("Standard Deviation: " + sd);
                    System.out.println("Intial Average: " + average);
                    System.out.println("-----------------------------------");
                    checkCorrectAverage();
                }
                else {
                    // Si no se encuentra ningún agente valido, empezamos un agente nuevo desde codigo
                    System.out.println("No agent found. Initializing new agent");
                    AgentContainer ac = myAgent.getContainerController();
                    if (enteredOnce) 
                        my_agent = ac.createNewAgent("term-started-code", "sid.prac1.Termometro", new Object[]{average, sd, 0.5, 1});
                    else
                        my_agent = ac.createNewAgent("term-started-code", "sid.prac1.Termometro", new Object[]{(a+b)/2, Math.sqrt(b-a), 0.5, 1});
                    my_agent.start();
                }
            } catch (Exception e) {}
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
        a = Double.parseDouble(args[0].toString());
        b = Double.parseDouble(args[1].toString());
        System.out.println(a);
        System.out.println(b);

        double ms = 1000;
        // Afegir behaviour a l'agent
        RecibirTemperaturas rt = new RecibirTemperaturas(this, Math.round(ms));
        this.addBehaviour(rt);
    }
}
