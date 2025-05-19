package com.notebridge.project.repository;

import com.notebridge.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//  By extending JpaRepository, you automatically get methods for:
//
//
//  CRUD operations (create, read, update, delete)
//  Paging and sorting
//  Query methods
//  For example, these methods are available without implementation:
//
//
//  save(User entity) - saves a User to the database
//  findById(Long id) - finds a User by ID
//  findAll() - gets all Users
//  delete(User entity) - deletes a User
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}
