package com.smartCity.Web.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.news.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {}
