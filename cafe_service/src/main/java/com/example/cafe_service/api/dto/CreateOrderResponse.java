package com.example.cafe_service.api.dto;

public record CreateOrderResponse(
  String globalId,
  String cliente,
  String tipo,
  String barista,
  String sucursal,
  double waitTime,
  double serviceTime,
  double totalTime,
  String error
) {}
