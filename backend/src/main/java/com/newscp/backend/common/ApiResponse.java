package com.newscp.backend.common;

public record ApiResponse<T>(int code, T data, String msg) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, data, "success");
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, null, "success");
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, null, msg);
    }
}
