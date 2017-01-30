package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Type("map")
public class Map {

  @Id
  private String id;
  private String author;
  private String battle_type;
  private LocalDateTime createTime;
  private String description;
  private String displayName;
  private URL downloadUrl;
  private URL thumbnailUrlSmall;
  private URL thumbnailUrlLarge;
  private int downloads;
  private String mapType;
  private int maxPlayers;
  private int numDraws;
  private Float rating;
  private String folderName;
  private int timesPlayed;
  private int version;
  private int sizeX;
  private int sizeY;
}
