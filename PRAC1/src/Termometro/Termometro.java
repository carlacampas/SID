package sid.termometro;

import java.util.*;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
FALTA: si algun behavior acaba, avisar
 */
public class Termometro extends Agent
{
    private float m, r, p, s;
    private float temp;

    public float getM() { return m; }
    public float getR() { return r; }
    public float getP() { return p; }
    public float getS() { return s; }

    public float getTemp() { return temp; }
    public void setTemp(float temp) { this.temp = temp; }

    public class Temperatura extends TickerBehaviour
    {
        public Temperatura(Agent a, long timeout)
        {
            super(a,timeout);
        }

        public void calcTemperature() {
            float prob = (float)Math.random();
            float max, min;

            if (prob < (p/100)) { min = m - 3*r; max = m + 3*r; }
            else { min = m - r; max = m + r; }

            setTemp((float)(Math.random() * (max - min)) + min);
        }

        public void onStart() { calcTemperature(); }

        public void onTick() { calcTemperature(); }
    }

    public class RecieveMessages extends CyclicBehaviour {
        MessageTemplate tpl;
        ACLMessage msg;

        public void onStart() {
            tpl = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        }

        public void action() {
            msg = myAgent.receive(tpl);
            if (msg != null) {
                String content = msg.getContent();
                if ((content != null) && (content.indexOf("temperature") != -1)) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(Float.toString(getTemp()));
                    send(reply);
                }
            }
            else {
                block();
            }
        }
    }

    protected void setup()
    {
        Object[] args = getArguments();
        if (args.length != 4) {
            System.out.println("Wrong number of parameters for thermometer inicialization. Arguments provided" +
                    args.length + "expected four.");
            System.exit(0);
        }
        m = Float.parseFloat(args[0].toString());
        r = Float.parseFloat(args[1].toString());
        p = Float.parseFloat(args[2].toString());
        s = Float.parseFloat(args[3].toString());
        float ms = s*1000;
        Temperatura t = new Temperatura(this, Math.round(ms));
        RecieveMessages rm = new RecieveMessages();
        this.addBehaviour(t);
        this.addBehaviour(rm);
    }
}
