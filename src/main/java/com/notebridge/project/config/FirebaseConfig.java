package com.notebridge.project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.storage-bucket}")
    private String storageBucket;

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @Bean
    FirebaseApp firebaseApp() throws IOException {
        ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
        InputStream serviceAccount = resource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(storageBucket)
                .build();

        return FirebaseApp.initializeApp(options);
    }
}