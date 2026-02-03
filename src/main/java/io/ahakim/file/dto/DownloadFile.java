package io.ahakim.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DownloadFile {

    private final String name;

    private final Long size;

    private final byte[] fileBytes;

    @Builder
    public DownloadFile(StoreFile storeFile, byte[] fileBytes) {
        this(storeFile.getName(), storeFile.getSize(), fileBytes);
    }

    public DownloadFile(String name, Long size, byte[] fileBytes) {
        this.name = name;
        this.size = size;
        this.fileBytes = fileBytes;
    }
}
