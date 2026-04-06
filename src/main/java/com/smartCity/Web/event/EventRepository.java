package com.smartCity.Web.event;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartCity.Web.event.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
