package org.upc.edu.Behaviours;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SearchPingBehaviourAgent extends Agent
{
    float a, b;
     protected void setup()
    {
        // envia request, termometro responde 
        /*
            Un agente termostato que, ya sea por push o por pull, obtenga las temperaturas de todos los term´ometros y
            tome una decisi´on sobre la temperatura actual bas´andose en un m´etodo que escoj´ais, e.g. la media aritm´etica.
            Este agente deber´ıa tener un estado interno que le permita
            detectar anomal´ıas en las lecturas de cada term´ometro y tomar decisiones
            sobre los term´ometros defectuosos, e.g. ignorarlos temporal o permanentemente. Adem´as, el termostato deber´
            ıa tener en cuenta que din´amicamente pueden entrar y salir term´ometros de la plataforma.

            Este agente debe aceptar dos argumentos enteros, que llamaremos a y b.
            Cuando la temperatura actual sea ≤ a o ≥ b el agente deber´a enviar un
            mensaje con performativa inform a todos los agentes de la plataforma con
            serviceDescription.type = ”alarm-management”
         */

        Object[] args = getArguments();
        if (args.length != 2) {
            System.out.println("Wrong number of parameters for thermometer inicialization.");
            exit();
        }
        float a = args[0].floatValue();
        float b = args[1].floatValue();
    }
}
