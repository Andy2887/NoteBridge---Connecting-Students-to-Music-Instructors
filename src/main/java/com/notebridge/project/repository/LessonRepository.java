package com.notebridge.project.repository;

import com.notebridge.project.model.Lesson;
import com.notebridge.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // Find lessons by teacher
    List<Lesson> findByTeacher(User teacher);

    // Find lessons by location type
    List<Lesson> findByLocation(String locationType);

    // Find upcoming lessons for a teacher
    List<Lesson> findByTeacherAndStartTimeAfter(User teacher, LocalDateTime now);

    // Find non-cancelled lessons
    List<Lesson> findByIsCancelledFalse();

    List<Lesson> findByTeacherAndIsCancelledFalse(User user);

    List<Lesson> findByStartTimeAfterAndIsCancelledFalse(LocalDateTime now);
}
