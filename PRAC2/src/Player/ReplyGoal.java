package examples.Player;

import bdi4jade.goal.Goal;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class ReplyGoal implements Goal {
  private Agent a;
  private AID player;
  private String choice;
  private ACLMessage msg;

  /*public SendGoal(Agent a, AID player, String choice, ACLMessage msg) {
    this.a = a;
    this.player = player;
    this.choice = choice;
    this.msg = msg;
  }*/

  public Agent getAgent() { return a; }
  public AID getPlayer() { return player; }
  public String getChoice() { return choice; }
  public ACLMessage getMessage() { return msg; }
}