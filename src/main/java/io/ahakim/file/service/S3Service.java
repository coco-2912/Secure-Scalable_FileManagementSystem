package io.ahakim.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public byte[] getObjectAsBytes(String keyName) {
        GetObjectRequest request = GetObjectRequest.builder()
                                                   .bucket(bucketName)
                                                   .key(keyName)
                                                   .build();
        return s3Client.getObjectAsBytes(request)
                       .asByteArray();

    }

    public void putObject(MultipartFile file, String keyName) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                                                   .bucket(bucketName)
                                                   .key(keyName)
                                                   .build();
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
    }

    public void deleteObject(String keyName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                                                         .bucket(bucketName)
                                                         .key(keyName)
                                                         .build();
        s3Client.deleteObject(request);
    }

    public void deleteObjects(List<String> keyNames) {
        List<ObjectIdentifier> keys =
                keyNames.stream()
                        .map(keyName -> ObjectIdentifier.builder().key(keyName).build())
                        .collect(Collectors.toList());

        Delete delete = Delete.builder()
                              .objects(keys)
                              .build();
        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                                                           .bucket(bucketName)
                                                           .delete(delete)
                                                           .build();
        s3Client.deleteObjects(request);
    }
}
