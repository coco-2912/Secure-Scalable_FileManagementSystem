package io.ahakim.file.service;

import io.ahakim.file.domain.AttachFile;
import io.ahakim.file.domain.User;
import io.ahakim.file.domain.UserRepository;
import io.ahakim.file.dto.DownloadFile;
import io.ahakim.file.dto.response.FileInfoResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final String bucketName = "pbl31-new-2026-dk";

    public List<FileInfoResponse> list(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        return user.getFiles().stream()
                .map(file -> new FileInfoResponse(file.getId(), file.getName(), file.getFolder(), file.getPassword() != null))
                .collect(Collectors.toList());
    }

    public DownloadFile download(Integer id, String providedPassword) {
        AttachFile file = userRepository.findFileById(id);
        if (file == null) {
            throw new IllegalArgumentException("File not found");
        }
        // Skip password check if providedPassword is null
        if (providedPassword != null && file.getPassword() != null && !file.getPassword().equals(providedPassword)) {
            throw new IllegalArgumentException("Incorrect password");
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getS3Key())
                .build();
        byte[] fileBytes = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        return new DownloadFile(file.getName(), file.getSize(), fileBytes);
    }

    public void upload(List<MultipartFile> files, Authentication authentication, String folder, String password) throws IOException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        for (MultipartFile multipartFile : files) {
            String originalFilename = multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            // Only include folder in s3Key if provided
            String folderPath = (folder != null && !folder.trim().isEmpty()) ? folder + "/" : "";
            String s3Key = email + "/" + folderPath + uuid + "_" + originalFilename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            AttachFile file = AttachFile.builder()
                    .uuid(uuid)
                    .name(originalFilename)
                    .size(multipartFile.getSize())
                    .s3Key(s3Key)
                    .folder(folder != null && !folder.trim().isEmpty() ? folder : null)
                    .password(password != null && !password.trim().isEmpty() ? password : null)
                    .user(user)
                    .build();
            user.getFiles().add(file);
        }
        userRepository.save(user);
    }

    public void delete(Integer id) {
        AttachFile file = userRepository.findFileById(id);
        if (file == null) {
            throw new IllegalArgumentException("File not found");
        }
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getS3Key())
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        file.getUser().getFiles().remove(file);
        userRepository.save(file.getUser());
    }
}