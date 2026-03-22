package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCityId(Long cityId);

    @Query("""
        SELECT e FROM Event e
        WHERE e.startDate >= CURRENT_TIMESTAMP
    """)
    List<Event> findUpcomingEvents();
}