package io.ahakim.file.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiUtil {

    public static <T> ApiResponse<T> success(T response) {
        return new ApiResponse<>(true, response, null);
    }

    public static ApiResponse<?> error(HttpStatus status, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(status, message));
    }

    @Getter
    public static class ErrorResponse {

        private final int status;
        private final String message;

        public ErrorResponse(HttpStatus status, String message) {
            this.status = status.value();
            this.message = message;
        }
    }

    @Getter
    public static class ApiResponse<T> {

        private final boolean success;
        private final T response;
        private final ErrorResponse error;

        public ApiResponse(boolean success, T response, ErrorResponse error) {
            this.success = success;
            this.response = response;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}