package com.notebridge.project.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.notebridge.project.model.FileMetaData;
import com.notebridge.project.model.Lesson;
import com.notebridge.project.model.User;
import com.notebridge.project.repository.FileMetaDataRepository;
import com.notebridge.project.repository.LessonRepository;
import com.notebridge.project.repository.UserRepository;
import com.notebridge.project.response.FileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Storage storage;
    private final FileMetaDataRepository repo;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final String bucketName = "notebridge-8c9a0.firebasestorage.app";

    @Autowired
    public FileStorageService(FileMetaDataRepository repo, UserRepository userRepository,
                              LessonRepository lessonRepository) throws IOException {
        this.repo = repo;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;

        ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
        InputStream serviceAccount = resource.getInputStream();

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        }

        String uniqueID = UUID.randomUUID().toString();
        String objectName = uniqueID + "_" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try {
            // Upload to Firebase
            storage.create(blobInfo, file.getBytes());

            // Save metadata to MySQL
            FileMetaData metaData = new FileMetaData();
            metaData.setUniqueId(uniqueID);
            metaData.setObjectName(objectName);
            metaData.setUploadDate(LocalDateTime.now());
            repo.save(metaData);

            return uniqueID;
        } catch (Exception e) {
            System.err.println("Data saved failed: " + e.getMessage() +"\ntrying to clean up db and firebase");
            try {
                storage.delete(blobId);
                int deleted = repo.deleteByUniqueId(uniqueID);
                if (deleted == 0) {
                    System.err.println("Warning: No records were deleted from the database for uniqueId: " + uniqueID);
                } else {
                    System.out.println("Successfully deleted " + deleted + " record(s) from database");
                }
            } catch (Exception ex) {
                System.err.println("Failed to clean up resources after upload failure: " + ex.getMessage());
            }
            throw e; // Re-throw the original exception
        }
    }
    @Transactional
    public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        }

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String uniqueID = UUID.randomUUID().toString();
        String objectName = uniqueID + "_" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try {
            // Upload to Firebase
            storage.create(blobInfo, file.getBytes());

            // Save metadata to MySQL with user association
            FileMetaData metaData = new FileMetaData();
            metaData.setUniqueId(uniqueID);
            metaData.setObjectName(objectName);
            metaData.setUploadDate(LocalDateTime.now());
            metaData.setUser(user);
            repo.save(metaData);

            return uniqueID;
        } catch (Exception e) {
            cleanupAfterFailedUpload(uniqueID, blobId, e);
            throw e; // Re-throw the original exception
        }
    }

    @Transactional
    public String uploadLessonPicture(MultipartFile file, Long lessonId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        }

        // Find the lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));

        String uniqueID = UUID.randomUUID().toString();
        String objectName = uniqueID + "_" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try {
            // Upload to Firebase
            storage.create(blobInfo, file.getBytes());

            // Save metadata to MySQL with lesson association
            FileMetaData metaData = new FileMetaData();
            metaData.setUniqueId(uniqueID);
            metaData.setObjectName(objectName);
            metaData.setUploadDate(LocalDateTime.now());
            metaData.setLesson(lesson);
            repo.save(metaData);

            return uniqueID;
        } catch (Exception e) {
            cleanupAfterFailedUpload(uniqueID, blobId, e);
            throw e; // Re-throw the original exception
        }
    }

    private void cleanupAfterFailedUpload(String uniqueID, BlobId blobId, Exception e) {
        System.err.println("Data saved failed: " + e.getMessage() + "\ntrying to clean up db and firebase");
        try {
            storage.delete(blobId);
            int deleted = repo.deleteByUniqueId(uniqueID);
            if (deleted == 0) {
                System.err.println("Warning: No records were deleted from the database for uniqueId: " + uniqueID);
            } else {
                System.out.println("Successfully deleted " + deleted + " record(s) from database");
            }
        } catch (Exception ex) {
            System.err.println("Failed to clean up resources after upload failure: " + ex.getMessage());
        }
    }

    public FileResponse retrieveFile(String fileId) {
        FileMetaData fileMetadata = repo.findByUniqueId(fileId);

        if (fileMetadata == null) {
            throw new IllegalArgumentException("No file found with the given ID: " + fileId);
        }

        String objectName = fileMetadata.getObjectName();
        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("No file found with the given ID: " + fileId);
        }

        FileResponse fileResponse = new FileResponse(objectName, blob.getContent());
        return fileResponse;
    }
}