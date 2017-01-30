package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Type("leaderboard_entry")
public class LeaderboardEntry {

  private String id;
  private String login;
  private float mean;
  private float deviation;
  private int numGames;
  private int wonGames;
  private boolean isActive;
  private int rating;
  private int ranking;
}
