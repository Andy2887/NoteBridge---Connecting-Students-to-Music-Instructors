package com.notebridge.project.service;

import com.notebridge.project.model.Chat;
import com.notebridge.project.model.Message;
import com.notebridge.project.model.User;
import com.notebridge.project.repository.ChatRepository;
import com.notebridge.project.repository.MessageRepository;
import com.notebridge.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create or get existing chat between teacher and student
     */
    @Transactional
    public Chat createOrGetChat(Long teacherId, Long studentId, String subject) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        // Validate roles
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher");
        }
        if (student.getRole() != User.Role.STUDENT) {
            throw new IllegalArgumentException("User is not a student");
        }
        
        // Check if chat already exists between these users
        Optional<Chat> existingChat = chatRepository.findByTeacherAndStudent(teacher, student);
        if (existingChat.isPresent()) {
            // Update subject if different
            Chat chat = existingChat.get();
            if (!chat.getSubject().equals(subject)) {
                chat.setSubject(subject);
                return chatRepository.save(chat);
            }
            return chat;
        }
        
        // Create new chat
        Chat chat = new Chat();
        chat.setTeacher(teacher);
        chat.setStudent(student);
        chat.setSubject(subject);
        
        return chatRepository.save(chat);
    }
    
    /**
     * Send a message in a chat
     */
    @Transactional
    public Message sendMessage(Long chatId, User sender, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        
        // Verify sender is part of the chat
        if (!chat.getTeacher().getId().equals(sender.getId()) && 
            !chat.getStudent().getId().equals(sender.getId())) {
            throw new IllegalArgumentException("User is not part of this chat");
        }
        
        // Create message
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        
        // Update chat's last message time
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
        
        return savedMessage;
    }
    
    /**
     * Get user's chats
     */
    public List<Chat> getUserChats(User user) {
        return chatRepository.findActiveByUser(user);
    }
    
    /**
     * Get chat messages (paginated)
     */
    public Page<Message> getChatMessages(Long chatId, User user, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        
        // Verify user is part of the chat
        if (!chat.getTeacher().getId().equals(user.getId()) && 
            !chat.getStudent().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User is not part of this chat");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByChatOrderBySentAtDesc(chat, pageable);
    }
    
    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long chatId, User user) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        
        messageRepository.markMessagesAsRead(chat, user);
    }
    
    /**
     * Get unread message count for user
     */
    public long getUnreadMessageCount(User user) {
        return messageRepository.countTotalUnreadMessages(user);
    }
}