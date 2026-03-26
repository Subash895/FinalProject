package com.smartCity.Web.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.smartCity.Web.Model.ForumPost;
import com.smartCity.Web.Service.ForumPostService;

@RestController
@RequestMapping("/api/forumposts")
@CrossOrigin(origins = "*")
public class ForumPostController {

    private final ForumPostService service;

    public ForumPostController(ForumPostService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ForumPost> create(@RequestBody ForumPost post) {
        return ResponseEntity.ok(service.create(post));
    }

    @GetMapping
    public ResponseEntity<List<ForumPost>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}