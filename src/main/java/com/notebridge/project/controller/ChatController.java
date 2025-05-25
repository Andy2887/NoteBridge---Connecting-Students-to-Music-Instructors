package com.notebridge.project.controller;

import com.notebridge.project.model.Chat;
import com.notebridge.project.model.Message;
import com.notebridge.project.model.User;
import com.notebridge.project.repository.UserRepository;
import com.notebridge.project.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@Validated
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Template for creating chat request
    public static class CreateChatRequest {
        @jakarta.validation.constraints.NotNull(message = "Teacher ID is required")
        public Long teacherId;
        
        @jakarta.validation.constraints.NotNull(message = "Student ID is required") 
        public Long studentId;
        
        @NotBlank(message = "Subject is required")
        @Size(max = 200, message = "Subject cannot exceed 200 characters")
        public String subject;
    }
    
    // Template for sending message request
    public static class SendMessageRequest {
        @NotBlank(message = "Message content is required")
        @Size(max = 1000, message = "Message cannot exceed 1000 characters")
        public String content;
    }
    
    /**
     * Create or get chat between teacher and student
     * POST /api/chats
     */
    @PostMapping
    public ResponseEntity<?> createChat(@Valid @RequestBody CreateChatRequest request) {
        try {
            User currentUser = getCurrentUser();
            
            // Only allow if current user is either the teacher or student
            if (!currentUser.getId().equals(request.teacherId) && 
                !currentUser.getId().equals(request.studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("You can only create chats you are part of");
            }
            
            Chat chat = chatService.createOrGetChat(
                request.teacherId, 
                request.studentId,
                request.subject
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(chat);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Error creating chat: " + e.getMessage());
        }
    }
    
    /**
     * Get current user's chats
     * GET /api/chats
     */
    @GetMapping
    public ResponseEntity<?> getMyChats() {
        try {
            User currentUser = getCurrentUser();
            List<Chat> chats = chatService.getUserChats(currentUser);
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error retrieving chats: " + e.getMessage());
        }
    }
    
    /**
     * Send message to chat
     * POST /api/chats/{chatId}/messages
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody SendMessageRequest request) {
        try {
            User currentUser = getCurrentUser();
            Message message = chatService.sendMessage(chatId, currentUser, request.content);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Get chat messages (paginated)
     * GET /api/chats/{chatId}/messages?page=0&size=20
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User currentUser = getCurrentUser();
            Page<Message> messages = chatService.getChatMessages(chatId, currentUser, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Error retrieving messages: " + e.getMessage());
        }
    }
    
    /**
     * Mark messages as read
     * PUT /api/chats/{chatId}/read
     */
    @PutMapping("/{chatId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long chatId) {
        try {
            User currentUser = getCurrentUser();
            chatService.markMessagesAsRead(chatId, currentUser);
            return ResponseEntity.ok("Messages marked as read");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Error marking messages as read: " + e.getMessage());
        }
    }
    
    /**
     * Get unread message count
     * GET /api/chats/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            User currentUser = getCurrentUser();
            long count = chatService.getUnreadMessageCount(currentUser);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error getting unread count: " + e.getMessage());
        }
    }
    
    // Helper method to get current authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }
}
