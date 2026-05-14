package com.team05.petmeeting.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.global.exception.ErrorResponse;
import com.team05.petmeeting.global.security.errorCode.SecurityErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = ErrorResponse.from(SecurityErrorCode.UNAUTHORIZED);

        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );
    }
}