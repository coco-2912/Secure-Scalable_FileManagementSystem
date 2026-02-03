package io.ahakim.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileInfoResponse {
    private Integer id;
    private String name;
    private String folder;
    private boolean isProtected;
    private int accessCount;
    private String lastAccessed;
    private boolean starred;
    private boolean isInTrash; // Add this field

    public FileInfoResponse(Integer id, String name, String folder, boolean isProtected) {
        this.id = id;
        this.name = name;
        this.folder = folder;
        this.isProtected = isProtected;
        this.accessCount = 0;
        this.lastAccessed = null;
        this.starred = false;
        this.isInTrash = false;
    }
}