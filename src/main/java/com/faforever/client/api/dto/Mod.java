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
@Type("mod")
public class Mod {

  @Id
  private String id;
  private String author;
  private String displayName;
  private String description;
  private int downloads;
  private int likes;
  private int timesPlayed;
  private URL downloadUrl;
  private URL thumbnailUrl;
  private boolean isRanked;
  private String type;
  private String version;
  private LocalDateTime createTime;

  public Mod() {
  }

  public Mod(String id, String displayName, String description, String author, LocalDateTime createTime) {
    this.id = id;
    this.displayName = displayName;
    this.description = description;
    this.author = author;
    this.createTime = createTime;
  }
}
