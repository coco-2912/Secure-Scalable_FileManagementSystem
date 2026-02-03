package io.ahakim.file.service;

import io.ahakim.file.domain.AttachFile;
import io.ahakim.file.domain.User;
import io.ahakim.file.domain.UserRepository;
import io.ahakim.file.dto.request.FileUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final UserRepository userRepository;

    public void insert(FileUploadRequest uploadFile, User user, String s3Key) {
        AttachFile file = uploadFile.toEntity(user, s3Key);
        user.getFiles().add(file);
        userRepository.save(user);
    }
}