package com.helpflow.dispatch.controller;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import com.helpflow.dispatch.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/technician-dispatch")
@Tag(name = "Technician Dispatch", description = "Simulates technician availability")
public class TechnicianDispatchController {

    private final DispatchService service;

    public TechnicianDispatchController(DispatchService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Get available technician for a ticket")
    public ResponseEntity<DispatchResponse> dispatch(@Valid @RequestBody DispatchRequest request) {
        return ResponseEntity.ok(service.dispatch(request));
    }
}
