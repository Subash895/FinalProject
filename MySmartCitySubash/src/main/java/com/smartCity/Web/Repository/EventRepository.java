package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartCity.Web.Model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}