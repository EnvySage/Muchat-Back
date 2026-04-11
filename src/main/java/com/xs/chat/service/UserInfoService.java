package com.xs.chat.service;

import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.UserDTO;
import com.xs.chat.pojo.VO.UserVO;

import java.util.List;

public interface UserInfoService {
    void register(UserDTO userDTO);

    UserVO login(UserDTO userDTO);

    UserVO SelectUserById(String idFromToken);

    boolean updateUserInfo(UserDO user);

}
