package com.faforever.client.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ErrorResponse {
  private final List<Error> errors;

  @Data
  public static class Error {
    private final int code;
    private final String title;
    private final String detail;
  }
}
