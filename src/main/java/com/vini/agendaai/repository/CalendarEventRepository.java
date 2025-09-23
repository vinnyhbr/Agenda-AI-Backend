package com.vini.agendaai.repository;

import com.vini.agendaai.model.CalendarEvent;
import com.vini.agendaai.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    Page<CalendarEvent> findByUserOrderByStartTimeDesc(User user, Pageable pageable);
    
    List<CalendarEvent> findByUserAndStartTimeBetweenOrderByStartTime(
        User user, LocalDateTime startTime, LocalDateTime endTime);
    
    Optional<CalendarEvent> findByGoogleEventId(String googleEventId);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.user = :user AND " +
           "((ce.startTime >= :startTime AND ce.startTime <= :endTime) OR " +
           "(ce.endTime >= :startTime AND ce.endTime <= :endTime) OR " +
           "(ce.startTime <= :startTime AND ce.endTime >= :endTime))")
    List<CalendarEvent> findOverlappingEvents(@Param("user") User user, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
}
