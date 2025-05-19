package com.notebridge.project.model;

import jakarta.persistence.*;
import lombok.Data;

// Marks this class as a JPA entity,
// meaning it will be mapped to a database table
@Entity

// Specifies the database table name for this entity
@Table(name = "users")

// This is a Lombok annotation (not JPA)
// that generates getters, setters.
@Data
public class User {

    @Id // Designates the field as the primary key
    // Configures how the primary key values are generated.
    // IDENTITY: auto-increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customizes the mapping between the field and the database column.
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    // Specifies that the enum should be persisted as a string value in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String instrument;

    @Column(length = 1000)
    private String bio;

    // Profile image could be stored as a path or URL
    private String profileImageUrl;

    // Additional fields
    private String firstName;
    private String lastName;
    private String phoneNumber;
}

enum Role {
    STUDENT,
    TEACHER
}