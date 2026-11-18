package com.nextface.common;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String error, String message, Map<String, String> details) {

    public ApiError(String error, String message) {
        this(error, message, null);
    }
}
