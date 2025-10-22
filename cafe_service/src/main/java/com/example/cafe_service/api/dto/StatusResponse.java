package com.example.cafe_service.api.dto;

public record StatusResponse(
  int branchId,
  int baristas,
  int queueSize,
  int completed
) {}
