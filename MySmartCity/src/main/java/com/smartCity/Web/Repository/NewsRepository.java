package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByCityId(Long cityId);

    List<News> findByIsGovernmentNoticeTrue();
}