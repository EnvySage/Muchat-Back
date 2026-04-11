package com.xs.chat.service.Impl;

import com.xs.chat.Utils.CheckCodeUtil;
import com.xs.chat.Utils.JWTUtils;
import com.xs.chat.Utils.StringUtil;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.UserDTO;
import com.xs.chat.pojo.VO.UserVO;
import com.xs.chat.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UserInfoImpl implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private JWTUtils jwtUtils;
    @Override
    public void register(UserDTO userDTO) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO, userDO);
        userDO.setId(StringUtil.generateUUID());
        userDO.setPasswordHash(StringUtil.encodeHash(userDTO.getPassword()));
        userDO.setAvatar("https://muyun-music.oss-cn-shenzhen.aliyuncs.com/user-avatar/xs.jpg");
        userDO.setCreatedAt(LocalDateTime.now());
        userDO.setUpdatedAt(LocalDateTime.now());
        userDO.setLastLoginAt(LocalDate.now());
        userDO.setStatus(1);
        userDO.setOnlineStatus(0);
        userInfoMapper.insertUserInfo(userDO);
    }

    @Override
    public UserVO login(UserDTO userDTO) {
        UserDO userDO = userInfoMapper.selectByNickname(userDTO.getNickname());
        UserVO userVO = new UserVO();
        if (userDO!=null&&StringUtil.verifyHash(userDTO.getPassword(),userDO.getPasswordHash())){
            BeanUtils.copyProperties(userDO, userVO);
            userVO.setToken(jwtUtils.generateToken(userDO.getId()));
            return userVO;
        }
        return userVO;
    }

    @Override
    public UserVO SelectUserById(String idFromToken) {
        UserDO userDO = userInfoMapper.selectById(idFromToken);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        userVO.setToken(jwtUtils.generateToken(userDO.getId()));
        return userVO;
    }

    @Override
    public boolean updateUserInfo(UserDO user) {
        int row = userInfoMapper.updateUserInfo(user);
        if (row > 0) {
            log.info("更新用户信息成功，userId:{}", user.getId());
            return true;
        } else {
            log.warn("更新用户信息失败，userId:{}", user.getId());
            return false;
        }
    }
}
