package examples.Player;

import java.util.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AID;

public class History {
  private AID player;
  private String last_selection;

  public History (AID player, String last_selection) {
    this.player = player;
    this.last_selection = last_selection;
  }

  public AID getPlayer() { return player; }
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    History h = (History) o;
    if (h.getPlayer().getName() == this.player.getName()) return true;
    return false;
  }

  public String getLast_selection(){return last_selection;}
}