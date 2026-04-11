package com.xs.chat.interceptor;

import com.xs.chat.Utils.JWTUtils;
import com.xs.chat.Utils.StringUtil;
import com.xs.chat.context.BaseContext;
import com.xs.chat.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthService authService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("请求拦截：{}，{}", request.getMethod(),requestURI);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if(StringUtil.isEmpty(token)||!token.startsWith("Bearer ")){
            log.info("token获取失败");
            response.setStatus(401);
            return false;
        }
        token = token.substring(7);
        String userId = authService.validateToken(token);
        if (userId == null) {
            log.info("Token验证失败");
            response.setStatus(401);
            return false;
        }
        BaseContext.setCurrentId(jwtUtils.getIdFromToken(token));
        log.info("设置当前用户id为{}", BaseContext.getCurrentId());
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
