package io.ahakim.file.dto;

public class ApiResponse<T> {
    private final boolean success;
    private final T response;
    private final String error;

    public ApiResponse(boolean success, T response, String error) {
        this.success = success;
        this.response = response;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public T getResponse() { return response; }
    public String getError() { return error; }
}