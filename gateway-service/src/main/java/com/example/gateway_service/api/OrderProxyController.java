package com.example.gateway_service.api;

import com.example.gateway_service.config.AppProps;
import com.example.gateway_service.core.RoundRobin;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderProxyController {
  private final WebClient http;
  private final RoundRobin rr;
  private final Duration timeout;
  private final List<String> backends;

  public OrderProxyController(WebClient http, AppProps props) {
    this.http = http;
    this.backends = props.backends();
    this.rr = new RoundRobin(this.backends);
    this.timeout = Duration.ofMillis(props.requestTimeoutMillis());
  }

  /** Devuelve el backend según branchId (1..N). Si es null, usa round-robin. */
  private String targetFor(Integer branchId) {
    if (branchId == null) return rr.next();
    int idx = branchId - 1;
    if (idx < 0 || idx >= backends.size()) {
      throw new IllegalArgumentException("branchId inválido: " + branchId);
    }
    return backends.get(idx);
  }

  /** Crear pedido: POST /api/orders[?branchId=2]  (si no mandas branchId => round-robin) */
  @PostMapping("/orders")
  public Mono<ResponseEntity<String>> createOrder(
      @RequestParam(required = false) Integer branchId,
      @RequestBody String body) {

    String url = targetFor(branchId).replaceAll("/+$", "") + "/api/orders";
    return http.post().uri(url)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(body)
        .retrieve()
        .toEntity(String.class)
        .timeout(timeout)
        .onErrorResume(e -> Mono.just(ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body("{\"error\":\"gateway_failed\",\"detail\":\"" + e.getMessage() + "\"}")));
  }

  /** Estado: GET /api/status[?branchId=2]
   *  - con branchId => consulta solo esa sucursal
   *  - sin branchId => agrega el estado de todas
   */
  @GetMapping("/status")
  public Mono<ResponseEntity<Object>> status(
      @RequestParam(required = false) Integer branchId) {

    if (branchId != null) {
      // Una sola sucursal
      try {
        String base = targetFor(branchId).replaceAll("/+$", "");
        return http.get().uri(base + "/api/status")
            .retrieve()
            .toEntity(Object.class)
            .timeout(timeout)
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("backend", base, "error", "down", "detail", e.getMessage()))));
      } catch (IllegalArgumentException iae) {
        return Mono.just(ResponseEntity.badRequest()
            .body(Map.of("error", "branchId_invalid", "detail", iae.getMessage())));
      }
    }

    // Agregado de todas las sucursales (N backends)
    return Flux.fromIterable(backends)
        .flatMap(b -> http.get().uri(b.replaceAll("/+$", "") + "/api/status")
            .retrieve()
            .bodyToMono(Object.class)
            .timeout(timeout)
            .onErrorReturn(Map.of("error", "down"))
            .map(status -> Map.of("base", b, "status", status)))
        .collectList()
        .map(list -> ResponseEntity.ok(Map.of(
            "backends", backends,
            "statuses", list
        )));
  }
}
