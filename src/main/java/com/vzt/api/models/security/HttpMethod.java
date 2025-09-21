package com.vzt.api.models.security;

import jakarta.servlet.http.HttpServletRequest;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH;

    public static HttpMethod fromRequest(HttpServletRequest request) {
        return HttpMethod.valueOf(request.getMethod());
    }
}
