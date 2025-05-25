package com.notebridge.project.repository;

import com.notebridge.project.model.Chat;
import com.notebridge.project.model.Message;
import com.notebridge.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Get messages for a chat (paginated)
    Page<Message> findByChatOrderBySentAtDesc(Chat chat, Pageable pageable);
    
    // Get latest messages for a chat
    List<Message> findTop50ByChatOrderBySentAtDesc(Chat chat);
    
    // Count unread messages for a user in a chat
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat AND m.sender != :user AND m.isRead = false")
    long countUnreadMessages(Chat chat, User user);
    
    // Mark messages as read
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.chat = :chat AND m.sender != :user AND m.isRead = false")
    void markMessagesAsRead(Chat chat, User user);
    
    // Count total unread messages for a user
    @Query("SELECT COUNT(m) FROM Message m WHERE (m.chat.teacher = :user OR m.chat.student = :user) AND m.sender != :user AND m.isRead = false")
    long countTotalUnreadMessages(User user);
}
