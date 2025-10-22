package com.example.coordinator_service.api;

import com.example.coordinator_service.api.dto.ReserveIdRequest;
import com.example.coordinator_service.api.dto.ReserveIdResponse;
import com.example.coordinator_service.core.IdGenerator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ids")
public class IdController {
  private final IdGenerator generator = new IdGenerator();

  @PostMapping("/reserve")
  public ReserveIdResponse reserve(@RequestBody ReserveIdRequest req) {
    String gid = generator.nextId(req.sid());
    return new ReserveIdResponse(gid);
  }
}
