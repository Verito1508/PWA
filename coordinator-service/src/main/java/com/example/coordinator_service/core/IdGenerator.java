package com.example.coordinator_service.core;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
  private final AtomicLong counter = new AtomicLong(0);

  public String nextId(int sid) {
    long c = counter.incrementAndGet();
    long ts = System.currentTimeMillis();
    return "G-" + c + "-T" + ts + "-S" + sid;
  }
}
