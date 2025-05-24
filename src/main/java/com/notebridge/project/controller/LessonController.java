package com.notebridge.project.controller;

import com.notebridge.project.model.Lesson;
import com.notebridge.project.model.User;
import com.notebridge.project.repository.LessonRepository;
import com.notebridge.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lessons")
@Validated
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    // GET ALL LESSONS - Available to all authenticated users
    @GetMapping
    public ResponseEntity<List<Lesson>> getAllLessons() {
        try {
            List<Lesson> lessons = lessonRepository.findByIsCancelledFalse();
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET LESSON BY ID - Available to all authenticated users
    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        try {
            Optional<Lesson> lesson = lessonRepository.findById(id);
            if (lesson.isPresent() && !lesson.get().isCancelled()) {
                return ResponseEntity.ok(lesson.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET LESSONS BY TEACHER - Available to all authenticated users
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Lesson>> getLessonsByTeacher(@PathVariable Long teacherId) {
        try {
            Optional<User> teacher = userRepository.findById(teacherId);
            if (teacher.isPresent() && teacher.get().getRole() == User.Role.TEACHER) {
                List<Lesson> lessons = lessonRepository.findByTeacherAndIsCancelledFalse(teacher.get());
                return ResponseEntity.ok(lessons);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET UPCOMING LESSONS - Available to all authenticated users
    @GetMapping("/upcoming")
    public ResponseEntity<List<Lesson>> getUpcomingLessons() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Lesson> lessons = lessonRepository.findByStartTimeAfterAndIsCancelledFalse(now);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // CREATE LESSON - Only teachers and admins can create lessons
    @PostMapping
    public ResponseEntity<String> createLesson(@Valid @RequestBody Lesson lesson) {
        try {
            // Get current authenticated user
            User currentUser = getCurrentUser();
            if (currentUser == null || (currentUser.getRole() != User.Role.TEACHER && currentUser.getRole() != User.Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("Only teachers and admins can create lessons");
            }

            // Validation
            if (lesson.getStartTime() == null || lesson.getEndTime() == null) {
                return ResponseEntity.badRequest().body("Start time and end time are required");
            }

            if (lesson.getStartTime().isAfter(lesson.getEndTime())) {
                return ResponseEntity.badRequest().body("Start time must be before end time");
            }

            if (lesson.getStartTime().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Cannot create lessons in the past");
            }

            // Set the teacher to the current authenticated user (if teacher) or assign to a specific teacher (if admin)
            if (currentUser.getRole() == User.Role.TEACHER) {
                lesson.setTeacher(currentUser);
            } else if (currentUser.getRole() == User.Role.ADMIN) {
                // Admin can assign lesson to any teacher, but if no teacher is specified, 
                // we need to validate that a teacher is provided in the request
                if (lesson.getTeacher() == null) {
                    return ResponseEntity.badRequest().body("Admin must specify a teacher for the lesson");
                }
                // Validate that the specified teacher actually exists and is a teacher
                Optional<User> teacherOpt = userRepository.findById(lesson.getTeacher().getId());
                if (!teacherOpt.isPresent() || teacherOpt.get().getRole() != User.Role.TEACHER) {
                    return ResponseEntity.badRequest().body("Invalid teacher specified");
                }
                lesson.setTeacher(teacherOpt.get());
            }
            lesson.setCancelled(false); // Ensure new lessons are not cancelled

            Lesson savedLesson = lessonRepository.save(lesson);
            return ResponseEntity.status(HttpStatus.CREATED)
                               .body("Lesson created successfully with ID: " + savedLesson.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error creating lesson: " + e.getMessage());
        }
    }

    // UPDATE LESSON - Only the teacher who created the lesson or admins can update it
    @PutMapping("/{id}")
    public ResponseEntity<String> updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson updatedLesson) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || (currentUser.getRole() != User.Role.TEACHER && currentUser.getRole() != User.Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("Only teachers and admins can update lessons");
            }

            Optional<Lesson> existingLessonOpt = lessonRepository.findById(id);
            if (!existingLessonOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Lesson existingLesson = existingLessonOpt.get();

            // Check if the current user is the teacher of this lesson OR an admin
            if (currentUser.getRole() != User.Role.ADMIN && 
                !existingLesson.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("You can only update your own lessons");
            }

            // Validation
            if (updatedLesson.getStartTime() != null && updatedLesson.getEndTime() != null) {
                if (updatedLesson.getStartTime().isAfter(updatedLesson.getEndTime())) {
                    return ResponseEntity.badRequest().body("Start time must be before end time");
                }
            }

            // Update fields (preserve teacher and ID)
            if (updatedLesson.getDescription() != null) {
                existingLesson.setDescription(updatedLesson.getDescription());
            }
            if (updatedLesson.getLocation() != null) {
                existingLesson.setLocation(updatedLesson.getLocation());
            }
            if (updatedLesson.getStartTime() != null) {
                existingLesson.setStartTime(updatedLesson.getStartTime());
            }
            if (updatedLesson.getEndTime() != null) {
                existingLesson.setEndTime(updatedLesson.getEndTime());
            }
            if (updatedLesson.getMeetingLink() != null) {
                existingLesson.setMeetingLink(updatedLesson.getMeetingLink());
            }
            if (updatedLesson.getPhysicalAddress() != null) {
                existingLesson.setPhysicalAddress(updatedLesson.getPhysicalAddress());
            }

            lessonRepository.save(existingLesson);
            return ResponseEntity.ok("Lesson updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error updating lesson: " + e.getMessage());
        }
    }

    // CANCEL LESSON - Only the teacher who created the lesson or admins can cancel it
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelLesson(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || (currentUser.getRole() != User.Role.TEACHER && currentUser.getRole() != User.Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("Only teachers and admins can cancel lessons");
            }

            Optional<Lesson> lessonOpt = lessonRepository.findById(id);
            if (!lessonOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Lesson lesson = lessonOpt.get();

            // Check if the current user is the teacher of this lesson OR an admin
            if (currentUser.getRole() != User.Role.ADMIN && 
                !lesson.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("You can only cancel your own lessons");
            }

            if (lesson.isCancelled()) {
                return ResponseEntity.badRequest().body("Lesson is already cancelled");
            }

            lesson.setCancelled(true);
            lessonRepository.save(lesson);
            return ResponseEntity.ok("Lesson cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error cancelling lesson: " + e.getMessage());
        }
    }

    // REACTIVATE LESSON - Only the teacher who created the lesson or admins can reactivate it
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivateLesson(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || (currentUser.getRole() != User.Role.TEACHER && currentUser.getRole() != User.Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("Only teachers and admins can reactivate lessons");
            }

            Optional<Lesson> lessonOpt = lessonRepository.findById(id);
            if (!lessonOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Lesson lesson = lessonOpt.get();

            // Check if the current user is the teacher of this lesson OR an admin
            if (currentUser.getRole() != User.Role.ADMIN && 
                !lesson.getTeacher().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("You can only reactivate your own lessons");
            }

            if (!lesson.isCancelled()) {
                return ResponseEntity.badRequest().body("Lesson is not cancelled");
            }

            lesson.setCancelled(false);
            lessonRepository.save(lesson);
            return ResponseEntity.ok("Lesson reactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error reactivating lesson: " + e.getMessage());
        }
    }

    // GET MY LESSONS - Teachers can see all their lessons, Admins can see all lessons
    @GetMapping("/my-lessons")
    public ResponseEntity<List<Lesson>> getMyLessons() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || (currentUser.getRole() != User.Role.TEACHER && currentUser.getRole() != User.Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Lesson> lessons;
            if (currentUser.getRole() == User.Role.ADMIN) {
                // Admins can see all lessons (including cancelled ones)
                lessons = lessonRepository.findAll();
            } else {
                // Teachers see only their own lessons
                lessons = lessonRepository.findByTeacher(currentUser);
            }
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET ALL LESSONS INCLUDING CANCELLED - Only admins can access this
    @GetMapping("/admin/all")
    public ResponseEntity<List<Lesson>> getAllLessonsIncludingCancelled() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body(null);
            }

            List<Lesson> lessons = lessonRepository.findAll();
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE LESSON PERMANENTLY - Only admins can permanently delete lessons
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLesson(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                   .body("Only admins can permanently delete lessons");
            }

            Optional<Lesson> lessonOpt = lessonRepository.findById(id);
            if (!lessonOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            lessonRepository.deleteById(id);
            return ResponseEntity.ok("Lesson deleted permanently");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error deleting lesson: " + e.getMessage());
        }
    }

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String username = authentication.getName();
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }
}
