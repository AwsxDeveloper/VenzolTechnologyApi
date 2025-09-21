package com.vzt.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status",ResponseStatus.FORBIDDEN);
        jsonResponse.put("message","You do not have enough permission to access!");
        jsonResponse.put("timestamp",LocalDateTime.now());

        response.getWriter().write(jsonResponse.toString());
    }

}
