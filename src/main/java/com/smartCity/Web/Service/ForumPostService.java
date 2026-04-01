package com.smartCity.Web.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import com.smartCity.Web.Model.ForumPost;
import com.smartCity.Web.Repository.CommentRepository;
import com.smartCity.Web.Repository.ForumPostRepository;

@Service
public class ForumPostService {

	private final ForumPostRepository repository;
	private final CommentRepository commentRepository;

	public ForumPostService(ForumPostRepository repository, CommentRepository commentRepository) {
		this.repository = repository;
		this.commentRepository = commentRepository;
	}

	public ForumPost create(ForumPost post) {

		if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
			throw new RuntimeException("Title required");
		}

		if (post.getContent() == null || post.getContent().trim().isEmpty()) {
			throw new RuntimeException("Content required");
		}

		return repository.save(post);
	}

	public List<ForumPost> getAll() {
		return repository.findAll();
	}

	public Optional<ForumPost> getById(Long id) {
		return repository.findById(id);
	}

	public ForumPost update(Long id, ForumPost post) {
		validate(post);

		ForumPost existing = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Forum post not found with id: " + id));

		existing.setTitle(post.getTitle().trim());
		existing.setContent(post.getContent().trim());
		return repository.save(existing);
	}

	@Transactional
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new RuntimeException("Forum post not found with id: " + id);
		}
		commentRepository.deleteByPostId(id);
		repository.deleteById(id);
	}

	private void validate(ForumPost post) {
		if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
			throw new RuntimeException("Title required");
		}

		if (post.getContent() == null || post.getContent().trim().isEmpty()) {
			throw new RuntimeException("Content required");
		}
	}
}
