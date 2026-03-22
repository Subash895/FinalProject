package com.smartCity.Web.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "forum_comments")
public class ForumComment extends BaseEntity {

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	// 🔥 CORRECT AUTHOR
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	@JsonIgnore
	private User author;

	// 🔥 LINK TO POST
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private ForumPost post;
}