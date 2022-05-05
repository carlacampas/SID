package examples.Player;;

import jade.lang.acl.ACLMessage;

public class QueueElem {

    private History hist;
    private ACLMessage msg;

    public QueueElem(){

        hist = null;
        msg = null;

    }

    public QueueElem(History h, ACLMessage m){

        hist = h;
        msg = m;

    }

    public History getHistory(){return hist;}
    public ACLMessage getMessage(){return msg;}

}
