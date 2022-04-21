package examples.Player;

import java.util.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AID;

public class History {
  private AID player;
  private String selection;
  private String last_play;

  public History (AID player, String selection) {
    this.player = player;
    this.selection = selection;
    this.last_play = "";
  }

  public void setLastPlay(String last_play) { this.last_play = last_play; }
  public String getLastPlay() { return this.last_play; }
  public String getSelection() { return selection; }

  public AID getPlayer() { return player; }
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    History h = (History) o;
    if (h.getPlayer().getName() == this.player.getName()) return true;
    return false;
  }
}