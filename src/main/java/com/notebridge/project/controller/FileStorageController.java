package com.notebridge.project.controller;

import com.notebridge.project.response.FileResponse;
import com.notebridge.project.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload/profile/{userId}")
    public ResponseEntity<String> uploadProfilePicture(
            @RequestParam MultipartFile file,
            @PathVariable Long userId) {
        try {
            String fileId = fileStorageService.uploadProfilePicture(file, userId);
            return ResponseEntity.ok(fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/upload/lesson_pic/{lessonId}")
    public ResponseEntity<String> uploadLessonPicture(
            @RequestParam MultipartFile file,
            @PathVariable Long lessonId) {
        try {
            String fileId = fileStorageService.uploadLessonPicture(file, lessonId);
            return ResponseEntity.ok(fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve/{fileId}")
    public ResponseEntity<FileResponse> retrieveFile(@PathVariable String fileId) {
        try {
            FileResponse fileResponse = fileStorageService.retrieveFile(fileId);
            return ResponseEntity.ok(fileResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}