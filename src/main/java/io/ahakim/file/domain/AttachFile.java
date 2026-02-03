package io.ahakim.file.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
public class AttachFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // This is Integer, not Long

    @Column(nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String s3Key;

    @Column
    private String folder;

    @Column
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public AttachFile(String uuid, String name, Long size, String s3Key, String folder, String password, User user) {
        this.uuid = uuid;
        this.name = name;
        this.size = size;
        this.s3Key = s3Key;
        this.folder = folder;
        this.password = password;
        this.user = user;
    }
}