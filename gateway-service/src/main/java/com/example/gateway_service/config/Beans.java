package com.example.gateway_service.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(AppProps.class) // carga gw.backends y gw.requestTimeoutMillis
public class Beans {

  @Bean
  WebClient webClient(AppProps props) {
    int ms = props.requestTimeoutMillis(); // del application.yml

    HttpClient http = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ms)   // timeout de conexión
        .responseTimeout(Duration.ofMillis(ms))             // timeout de respuesta
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(ms, TimeUnit.MILLISECONDS))  // lectura
            .addHandlerLast(new WriteTimeoutHandler(ms, TimeUnit.MILLISECONDS)) // escritura
        );

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(http))
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024)) // 4MB
                .build()
        )
        .build();
  }
}
