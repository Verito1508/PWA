package com.example.cafe_service.api;

import com.example.cafe_service.api.dto.StatusResponse;
import com.example.cafe_service.core.BaristaPool;
import org.springframework.web.bind.annotation.*;

@RestController
public class StatusController {
  private final BaristaPool pool;
  public StatusController(BaristaPool pool) { this.pool = pool; }

  @GetMapping("/api/status")
  public StatusResponse status() {
    return new StatusResponse(
      pool.branchId(),
      pool.baristasActivos(),
      pool.queueSize(),
      pool.completed()
    );
  }
}
