package com.smartCity.Web.forum;

import jakarta.persistence.*;

@Entity
@Table(name = "forum_posts")
public class ForumPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String content;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	//
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
