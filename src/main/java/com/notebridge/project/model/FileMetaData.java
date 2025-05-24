package com.notebridge.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "unique_id", nullable = false)
    private String uniqueId;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    // “Map this User object to a column named user_id that stores the user's primary key (id)”.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    public FileMetaData() {}

    public FileMetaData(Integer id, String uniqueId, String objectName, LocalDateTime uploadDate) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.objectName = objectName;
        this.uploadDate = uploadDate;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getObjectName() {
        return objectName;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
}
