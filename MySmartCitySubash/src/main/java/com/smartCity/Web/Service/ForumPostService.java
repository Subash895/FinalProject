package com.smartCity.Web.Service;

import org.springframework.stereotype.Service;
import java.util.List;

import com.smartCity.Web.Model.ForumPost;
import com.smartCity.Web.Repository.ForumPostRepository;

@Service
public class ForumPostService {

	private final ForumPostRepository repository;

	public ForumPostService(ForumPostRepository repository) {
		this.repository = repository;
	}

	public ForumPost create(ForumPost post) {
		return repository.save(post);
	}

	public List<ForumPost> getAll() {
		return repository.findAll();
	}
}