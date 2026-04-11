package com.xs.chat.service.Impl;

import com.xs.chat.Utils.JWTUtils;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthImpl implements AuthService {
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Override
    public String validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        if (!jwtUtils.validateToken(token)) {
            log.info("Token验证失败");
            return null;
        }
        String userId = jwtUtils.getIdFromToken(token);

        UserDO user = userInfoMapper.selectById(userId);
        if (user == null) {
            log.info("用户不存在 - userId: {}", userId);
            return null;
        }
        return userId;
    }
}
