package com.example.gateway_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gw")
public record AppProps(
  List<String> backends,        // ej: ["http://localhost:","http://localhost:8083"]
  int requestTimeoutMillis      // ej: 30000
) {}
