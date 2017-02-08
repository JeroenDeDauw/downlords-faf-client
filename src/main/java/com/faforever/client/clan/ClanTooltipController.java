package com.faforever.client.clan;


import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

@Controller
public class ClanTooltipController {
  public VBox root;
  public Label clanName;
  public Label clanDescription;
  public Label clanMembers;
  public Label clanLeader;
  private Clan clan;

  public void setClan(Clan clan) {
    this.clan = clan;
    clanName.setText(clan.getClanName());
    clanDescription.setText(clan.getDescription());
    clanLeader.setText(clan.getLeaderName());
    clanMembers.setText(clan.getClanMembers().toString());
  }

  public VBox getRoot() {
    return root;
  }

}
