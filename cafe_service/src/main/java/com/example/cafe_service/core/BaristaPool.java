package com.example.cafe_service.core;

import com.example.cafe_service.config.AppProps;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class BaristaPool {
  private final AppProps props;
  private final WebClient client;
  private final ArrayBlockingQueue<Pedido> cola = new ArrayBlockingQueue<>(50);
  private final Random rnd = new Random();
  private final AtomicInteger atendidos = new AtomicInteger(0);
 
  public int queueSize() { return cola.size(); }
  public int completed() { return atendidos.get(); }
  public int baristasActivos() { return props.baristas(); }
  public int branchId() { return props.branchId(); }

  public BaristaPool(AppProps props, WebClient client) {
    this.props = props; this.client = client;
    for (int i=1; i<=props.baristas(); i++) {
      final int bid = i;
      Thread t = new Thread(() -> loop(bid), "Barista-"+bid);
      t.setDaemon(true); t.start();
    }
  }

  public boolean encolar(Pedido p) { return cola.offer(p); }

  private double tserv(boolean lento) {
    double base = lento
      ? rand(props.rangoLentoMin(), props.rangoLentoMax())
      : rand(props.rangoRapidoMin(), props.rangoRapidoMax());
    return base * props.factorTiempo();
  }
  private double rand(double a, double b) { return a + rnd.nextDouble()*(b-a); }

  private Mono<String> reservarId() {
    record Req(int sid) {}
    record Res(String globalId) {}
    return client.post().uri("/api/ids/reserve")
      .bodyValue(new Req(props.branchId()))
      .retrieve().bodyToMono(Res.class)
      .map(Res::globalId);
  }

  private void loop(int bid) {
    while (true) {
      try {
        Pedido p = cola.take();
        long tStart = System.currentTimeMillis();
        String gid;
        try { gid = reservarId().block(); }
        catch (Exception e) {
          p.res = new Pedido.Result(null, p.cliente, p.tipo, "Barista-"+bid,
            0,0,0,"S"+props.branchId(), "coord_unreachable: "+e.getMessage());
          p.latch.countDown(); continue;
        }
        double serv = tserv(p.lento);
        Thread.sleep((long)(serv*1000));
        long tEnd = System.currentTimeMillis();
        double wait = (tStart - p.tArr)/1000.0;
        double total = (tEnd - p.tArr)/1000.0;

        p.res = new Pedido.Result(gid, p.cliente, p.tipo, "Barista-"+bid,
        round1(wait), round1(serv), round1(total), "S"+props.branchId(), null);
        atendidos.incrementAndGet();
        p.latch.countDown();
      } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
    }
  }
  private double round1(double v){ return Math.round(v*10.0)/10.0; }
}
