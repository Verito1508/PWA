package com.example.gateway_service.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin {
  private final AtomicInteger idx = new AtomicInteger(0);
  private final List<String> targets;

  public RoundRobin(List<String> targets) {
    if (targets == null || targets.isEmpty())
      throw new IllegalArgumentException("targets vacíos");
    this.targets = targets;
  }

  public String next() {
    int i = Math.floorMod(idx.getAndIncrement(), targets.size());
    return targets.get(i);
  }

  public List<String> all() { return targets; }
}
