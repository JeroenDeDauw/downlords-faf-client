package com.faforever.client.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class History {

  private java.util.Map<String, List<Float>> data;

  public Map<String, List<Float>> getData() {
    return data;
  }
}
