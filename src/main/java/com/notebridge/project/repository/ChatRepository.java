package com.notebridge.project.repository;

import com.notebridge.project.model.Chat;
import com.notebridge.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    // Find all chats for a user (teacher or student)
    @Query("SELECT c FROM Chat c WHERE c.teacher = :user OR c.student = :user ORDER BY c.lastMessageAt DESC")
    List<Chat> findByUser(User user);
    
    // Find active chats for a user
    @Query("SELECT c FROM Chat c WHERE (c.teacher = :user OR c.student = :user) AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    List<Chat> findActiveByUser(User user);
    
    // Find chat between specific teacher and student
    Optional<Chat> findByTeacherAndStudent(User teacher, User student);
    
    // Find chats for a teacher
    List<Chat> findByTeacherOrderByLastMessageAtDesc(User teacher);
    
    // Find chats for a student
    List<Chat> findByStudentOrderByLastMessageAtDesc(User student);
}
