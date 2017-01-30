package com.faforever.client.api.dto;


import com.github.jasminb.jsonapi.annotations.Id;
import lombok.Data;

@Data
public class PlayerAchievement {

  @Id
  private String id;
  private AchievementState state;
  private String achievementId;
  private Integer currentSteps;
  private String createTime;
  private String updateTime;
}
