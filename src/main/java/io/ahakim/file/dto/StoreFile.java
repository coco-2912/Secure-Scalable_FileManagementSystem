package io.ahakim.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreFile {

    private final Integer id;

    private final String uuid;

    private final String name;

    private final Long size;

    @Builder
    public StoreFile(Integer id, String uuid, String name, Long size) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.size = size;
    }
}
