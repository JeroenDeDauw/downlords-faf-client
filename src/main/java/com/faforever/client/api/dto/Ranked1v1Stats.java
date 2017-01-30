package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Type("ranked_1v1_stats")
public class Ranked1v1Stats {

  @Id
  private String id;
  private Map<String, Integer> ratingDistribution;
}
