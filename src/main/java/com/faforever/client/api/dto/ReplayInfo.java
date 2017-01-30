package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import javafx.collections.ObservableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Type("replay_info")
public class ReplayInfo {
  @Id
  private int id;
  private String title;
  private String featuredModId;
  private String mapId;
  private Instant startTime;
  private Instant endTime;
  private int views;
  private ObservableMap<String, List<String>> teams;
}
