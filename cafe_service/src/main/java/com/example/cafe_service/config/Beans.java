package com.example.cafe_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AppProps.class)
public class Beans {
  @Bean
  WebClient webClient(AppProps props) {
    return WebClient.builder().baseUrl(props.coordinatorBaseUrl()).build();
  }
}
