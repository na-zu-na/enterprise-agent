package org.cc.enterpriseagent.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, 401, "MISSING_TOKEN", "未提供有效的 Authorization Token");
            return false;
        }

        String token = authorization.substring(7);

        // 校验 JWT 本身
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, 401, "TOKEN_INVALID", "Token 无效或已过期");
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        request.setAttribute("CURRENT_USER_ID", userId);
        request.setAttribute("TOKEN",token);

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        // 设置状态码
        response.setStatus(status);

        // 设置 Content-Type 为 JSON
        response.setContentType("application/json;charset=UTF-8");

        // 处理跨域头
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // 构建统一的错误 JSON 结构
        String jsonResponse = String.format(
                "{\"code\": %d, \"errorCode\": \"%s\", \"message\": \"%s\", \"data\": null}",
                status, code, message
        );

        // 5. 写入输出流
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
