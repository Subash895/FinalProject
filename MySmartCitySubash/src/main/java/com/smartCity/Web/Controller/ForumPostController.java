package com.smartCity.Web.Controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.smartCity.Web.Model.ForumPost;
import com.smartCity.Web.Service.ForumPostService;

@RestController
@RequestMapping("/api/forumposts")
@CrossOrigin
public class ForumPostController {

	private final ForumPostService service;

	public ForumPostController(ForumPostService service) {
		this.service = service;
	}

	@PostMapping
	public ForumPost create(@RequestBody ForumPost post) {
		return service.create(post);
	}

	@GetMapping
	public List<ForumPost> getAll() {
		return service.getAll();
	}
}