package com.faforever.client.config;

import com.google.common.eventbus.EventBus;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This configuration has to be imported by other configurations and should only contain beans that are necessary to run
 * the application.
 */
@org.springframework.context.annotation.Configuration
@PropertySource("classpath:/application.properties")
public class BaseConfig {

  private final ScheduledExecutorService scheduledExecutorService;

  @Inject
  public BaseConfig(ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
  }

  @Bean
  static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  Locale locale() {
    return Locale.getDefault();
  }

  @Bean
  MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("i18n.messages");
    return messageSource;
  }

  @Bean
  ThreadPoolExecutor threadPoolExecutor() {
    return new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors() * 4, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
  }

  @Bean
  ScheduledExecutorService scheduledExecutorService() {
    return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
  }

  @PreDestroy
  void shutdown() {
    scheduledExecutorService.shutdown();
  }

  @Bean
  EventBus eventBus() {
    return new EventBus((exception, context) -> exception.printStackTrace());
  }
}
