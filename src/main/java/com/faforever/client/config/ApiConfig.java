package com.faforever.client.config;

import com.faforever.client.api.JsonApiMessageConverter;
import com.github.jasminb.jsonapi.ResourceConverter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;

import javax.inject.Inject;

@Configuration
public class ApiConfig {

  @Bean
  public OAuth2ClientContext oauth2ClientContext() {
    return new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest());
  }

  @Bean
  public ResourceConverter resourceConverter() {
    return new ResourceConverter();
  }

  @Inject
  public void restTemplateBuilder(RestTemplateBuilder restTemplateBuilder, JsonApiMessageConverter jsonApiMessageConverter, ClientProperties clientProperties) {
    restTemplateBuilder
        .messageConverters(jsonApiMessageConverter)
        .rootUri(clientProperties.getApi().getBaseUrl());
  }
}
