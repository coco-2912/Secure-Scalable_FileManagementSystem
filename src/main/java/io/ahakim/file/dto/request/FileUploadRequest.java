package io.ahakim.file.dto.request;

import io.ahakim.file.domain.AttachFile;
import io.ahakim.file.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FileUploadRequest {
    private final String uuid;
    private final String name;
    private final Long size;
    private final String folder;
    private final String password;

    @Builder
    public FileUploadRequest(String uuid, String name, Long size, String folder, String password) {
        this.uuid = uuid;
        this.name = name;
        this.size = size;
        this.folder = folder;
        this.password = password;
    }

    public AttachFile toEntity(User user, String s3Key) {
        return AttachFile.builder()
                .uuid(uuid)
                .name(name)
                .size(size)
                .s3Key(s3Key)
                .folder(folder)
                .password(password)
                .user(user)
                .build();
    }
}