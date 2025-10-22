package com.example.cafe_service.core;

import java.util.concurrent.CountDownLatch;

public class Pedido {
  public final String cliente;
  public final String tipo;
  public final long tArr;
  public final boolean lento;
  public volatile Result res;
  public final CountDownLatch latch = new CountDownLatch(1);

  public record Result(
      String globalId,
      String cliente,
      String tipo,
      String barista,
      double waitTime,
      double serviceTime,
      double totalTime,
      String sucursal,
      String error
  ) {}

  public Pedido(String cliente, String tipo, boolean lento) {
    this.cliente = cliente;
    this.tipo = tipo;
    this.lento = lento;
    this.tArr = System.currentTimeMillis();
  }
}
