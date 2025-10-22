package com.example.cafe_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cafe")
public record AppProps(
  int branchId,
  int baristas,
  double pLento,
  double rangoRapidoMin,
  double rangoRapidoMax,
  double rangoLentoMin,
  double rangoLentoMax,
  double factorTiempo,
  String coordinatorBaseUrl
) {}
