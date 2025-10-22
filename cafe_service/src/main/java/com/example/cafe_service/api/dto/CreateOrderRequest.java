package com.example.cafe_service.api.dto;

public record CreateOrderRequest(String cliente, String tipo, Double timeout) {}
