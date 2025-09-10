package com.vini.agendaai.repository;

import com.vini.agendaai.model.ChatMessage;
import com.vini.agendaai.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
    
    List<ChatMessage> findTop10ByUserOrderByCreatedAtDesc(User user);
}
