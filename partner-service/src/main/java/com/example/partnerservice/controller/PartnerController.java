package com.example.partnerservice.controller;

import com.example.partnerservice.model.Partner;
import com.example.partnerservice.repository.PartnerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/partners")
public class PartnerController {
    private final PartnerRepository repo;

    public PartnerController(PartnerRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<Partner> create(@RequestBody Partner p) {
        p.setStatus("AVAILABLE");
        return ResponseEntity.ok(repo.save(p));
    }

    @GetMapping
    public List<Partner> list() { return repo.findAll(); }

    @PostMapping("/{id}/location")
    public ResponseEntity<Partner> updateLocation(@PathVariable Long id, @RequestBody Partner loc) {
        Optional<Partner> op = repo.findById(id);
        if (op.isEmpty()) return ResponseEntity.notFound().build();
        var p = op.get();
        p.setLat(loc.getLat());
        p.setLng(loc.getLng());
        repo.save(p);
        return ResponseEntity.ok(p);
    }
}
