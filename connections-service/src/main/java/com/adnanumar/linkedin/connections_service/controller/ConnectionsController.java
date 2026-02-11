package com.adnanumar.linkedin.connections_service.controller;

import com.adnanumar.linkedin.connections_service.entity.Person;
import com.adnanumar.linkedin.connections_service.service.ConnectionsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionsController {

    final ConnectionsService connectionsService;

    @GetMapping("/first-degree")
    public ResponseEntity<List<Person>> getFirstConnections() {
        return ResponseEntity.ok(connectionsService.getFirstDegreeConnections());
    }

}
