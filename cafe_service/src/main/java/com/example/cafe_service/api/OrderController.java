package com.example.cafe_service.api;

import com.example.cafe_service.api.dto.CreateOrderRequest;
import com.example.cafe_service.api.dto.CreateOrderResponse;
import com.example.cafe_service.config.AppProps;
import com.example.cafe_service.core.BaristaPool;
import com.example.cafe_service.core.Pedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final BaristaPool pool;
  private final AppProps props;
  private final Random rnd = new Random();

  public OrderController(BaristaPool pool, AppProps props) {
    this.pool = pool; this.props = props;
  }

  @PostMapping
  public ResponseEntity<CreateOrderResponse> create(@RequestBody CreateOrderRequest req) throws InterruptedException {
    String cliente = (req.cliente()==null || req.cliente().isBlank()) ? "Cliente-??" : req.cliente();
    String tipo = (req.tipo()==null || req.tipo().isBlank()) ? "Latte" : req.tipo();
    double timeout = (req.timeout()==null ? 60.0 : req.timeout());

    boolean lento = rnd.nextDouble() < props.pLento();
    Pedido p = new Pedido(cliente, tipo, lento);
    if (!pool.encolar(p)) {
      return ResponseEntity.ok(new CreateOrderResponse(null, cliente, tipo, null, "S"+props.branchId(), 0,0,0,"cola_llena"));
    }

    boolean ok = p.latch.await((long)(timeout*1000), TimeUnit.MILLISECONDS);
    if (!ok) {
      return ResponseEntity.ok(new CreateOrderResponse(null, cliente, tipo, null, "S"+props.branchId(), 0,0,0,"timeout"));
    }

    var r = p.res;
    return ResponseEntity.ok(new CreateOrderResponse(
    r.globalId(),
    r.cliente(),
    r.tipo(),
    r.barista(),
    r.sucursal(),
    r.waitTime(),
    r.serviceTime(),
    r.totalTime(),
    r.error()
    ));

  }
}
