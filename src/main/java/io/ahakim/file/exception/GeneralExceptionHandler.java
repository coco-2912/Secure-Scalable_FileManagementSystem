package io.ahakim.file.exception;

import io.ahakim.file.util.ApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
@RestControllerAdvice(basePackages = "io.ahakim.file.controller") // Apply only to REST controllers
public class GeneralExceptionHandler {

    private ResponseEntity<ApiUtil.ApiResponse<?>> newResponse(HttpStatus status, Throwable throwable) {
        return newResponse(status, throwable.getMessage());
    }

    private ResponseEntity<ApiUtil.ApiResponse<?>> newResponse(HttpStatus status, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(ApiUtil.error(status, message), headers, status);
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<?> S3Exception(S3Exception e) {
        String errorMessage = "S3Exception occurred.";
        if (e instanceof NoSuchKeyException)
            errorMessage = "키에 해당하는 오브젝트를 찾을 수 없음";

        log.debug("⚠S3Exception occurred: {}", e.getMessage(), e);
        return newResponse(HttpStatus.NOT_FOUND, errorMessage);
    }

    @ExceptionHandler(SdkException.class)
    public ResponseEntity<?> AmazonException(SdkException e) {
        String message = e.getMessage();
        if (e instanceof AwsServiceException)
            message = String.valueOf(((AwsServiceException) e).awsErrorDetails());

        log.debug("⚠️AmazonException: {}", message, e);
        return newResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Amazon Web Service exception occurred.");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> IOException(IOException e) {
        log.debug("⚠️IOException: {}", e.getMessage(), e);
        return newResponse(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> MultipartException(MultipartException e) {
        log.debug("⚠️MultipartException: {}", e.getMessage(), e);
        return newResponse(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<?> HttpMediaTypeException(HttpMediaTypeException e) {
        log.debug("⚠HttpMediaTypeException: {}", e.getMessage(), e);
        return newResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> MethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        log.debug("⚠HttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
        return newResponse(HttpStatus.METHOD_NOT_ALLOWED, e);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> NoHandlerFoundException(NoHandlerFoundException e) {
        log.debug("⚠NoHandlerFoundException: {}", e.getMessage(), e);
        return newResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<?> Exception(Exception e) {
        log.debug("Bad request exception occurred: {}", e.getMessage(), e);
        return newResponse(HttpStatus.BAD_REQUEST, e);
    }
}