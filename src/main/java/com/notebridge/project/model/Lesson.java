package com.notebridge.project.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String meetingLink;

    private String physicalAddress;

    private boolean isCancelled = false;
}

enum LocationType {
    ONLINE,
    IN_PERSON,
    HYBRID
}
